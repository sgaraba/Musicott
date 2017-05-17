/*
 * This file is part of Musicott software.
 *
 * Musicott software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Musicott library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Musicott. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2015 - 2017 Octavio Calleya
 */

package com.transgressoft.musicott;

import com.google.inject.*;
import com.sun.javafx.application.*;
import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.player.*;
import com.transgressoft.musicott.services.*;
import com.transgressoft.musicott.tasks.*;
import com.transgressoft.musicott.tasks.load.*;
import com.transgressoft.musicott.util.*;
import com.transgressoft.musicott.util.guice.factories.*;
import com.transgressoft.musicott.util.guice.modules.*;
import com.transgressoft.musicott.view.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.stage.Stage;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static com.transgressoft.musicott.MainPreloader.*;
import static com.transgressoft.musicott.model.Layout.*;

/**
 * Creates and launches Musicott. The creation of the application follows this steps:
 * <ol>
 * <li>Initialization of the {@code Logger}</li>
 * <li>The {@link MainPreloader} is created and shown</li>
 * <li>It is checked whether it is the first use, and if so, the user enters the application folder</li>
 * <li>Configuration properties, tracks, playlists and waveforms are loaded</li>
 * <li>The main window is shown</li>
 * </ol>
 *
 * @author Octavio Calleya
 * @version 0.10-b
 * @see <a href="https://octaviospain.github.io/Musicott">Musicott</a>
 */
public class MusicottApplication extends Application implements InjectedApplication {

    private static final String CONFIG_FILE = "resources/config/config.properties";

    static Injector injector = Guice.createInjector(new LoaderModule());

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private TaskDemon taskDemon;

    public static void main(String[] args) {
        Utils.initializeLogger();
        LauncherImpl.launchApplication(MusicottApplication.class, MainPreloader.class, args);
    }

    @Override
    public void init() throws Exception {
        injector = injector.createChildInjector(new HostServicesModule(getHostServices()));
        MainPreferences preferences = injector.getInstance(MainPreferences.class);
        String applicationFolder = preferences.getMusicottUserFolder();
        if (isFirstUse(applicationFolder))
            LauncherImpl.notifyPreloader(this, new CustomProgressNotification(0, FIRST_USE_EVENT));

        LastFmPreferences lastFmPreferences = injector.getInstance(LastFmPreferences.class);
        loadConfigProperties(lastFmPreferences);

        taskDemon = injector.getInstance(TaskDemon.class);
        taskDemon.deactivateSaveLibrary();
        loadPersistedData(applicationFolder);
        taskDemon.activateSaveLibrary();
    }

    private boolean isFirstUse(String userFolder) {
        boolean res = false;
        if (userFolder == null || ! new File(userFolder).exists())
            res = true;
        return res;
    }

    /**
     * Loads required configuration parameters from a {@code .properties} file
     */
    private void loadConfigProperties(LastFmPreferences servicePreferences) {
        Properties properties = new Properties();
        try {
            BaseLoadAction.notifyPreloader(0, 4, "Loading configuration...", this);
            properties.load(new FileInputStream(CONFIG_FILE));
            String apiKey = properties.getProperty("lastfm_api_key");
            String apiSecret = properties.getProperty("lastfm_api_secret");
            servicePreferences.setApiSecret(apiSecret);
            servicePreferences.setApiKey(apiKey);
        }
        catch (IOException exception) {
            LOG.warn("Error loading configuration properties", exception);
        }
    }

    private void loadPersistedData(String applicationFolder) {
        LoadActionFactory loadActionFactory = injector.getInstance(LoadActionFactory.class);
        ForkJoinPool loadForkJoinPool = new ForkJoinPool(4);
        loadForkJoinPool.invoke(loadActionFactory.createWaveformsAction(applicationFolder, this));
        loadForkJoinPool.invoke(loadActionFactory.createPlaylistAction(applicationFolder, this));
        loadForkJoinPool.invoke(loadActionFactory.createTracksAction(null, - 1, applicationFolder, this));
        loadForkJoinPool.shutdown();
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public void start(Stage primaryStage) throws IOException, ReflectiveOperationException {
        ControllerModule<ErrorDialogController> errorModule = loadControllerModule(ERROR_DIALOG);
        taskDemon.setErrorDialog(errorModule.providesController());

        ControllerModule<PreferencesController> prefsModule = loadControllerModule(PREFERENCES);
        ControllerModule<SimpleProgressBarController> progressModule = loadControllerModule(PROGRESS_BAR);
        ControllerModule<PlayQueueController> playQueueModule = loadControllerModule(PLAY_QUEUE, primaryStage);
        injector = injector.createChildInjector(new WaveformPaneFactoryModule());

        ControllerModule<PlayerController> playerModule = loadControllerModule(PLAYER, primaryStage);
        injector = injector.createChildInjector(playerModule);

        ControllerModule<ArtistsViewController> artistsModule = loadControllerModule(ARTISTS, primaryStage);
        ControllerModule<NavigationController> navigationModule = loadControllerModule(NAVIGATION, primaryStage);
        injector = injector.createChildInjector(errorModule, prefsModule, progressModule, playQueueModule,
                                                artistsModule, navigationModule);

        ControllerModule<RootMenuBarController> menuBarModule = loadControllerModule(MENU_BAR, primaryStage);
        injector = injector.createChildInjector(menuBarModule);

        ControllerModule<RootController> rootModule = loadControllerModule(ROOT, primaryStage);
        RootController rootController = rootModule.providesController();
        injector = injector.createChildInjector(rootModule, new MusicottModule());

        ControllerModule<EditController> editModule = loadControllerModule(EDITION);
        injector = injector.createChildInjector(editModule, new TrackSetAreaRowFactoryModule());

        wireViewsAndSingletons(artistsModule, menuBarModule, navigationModule, rootModule);

        primaryStage.setScene(new Scene(rootController.getRoot()));
        primaryStage.setOnCloseRequest(event -> {
            LOG.info("Exiting Musicott");
            taskDemon.shutDownTasks();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void wireViewsAndSingletons(ControllerModule... modules) {
        PlayerFacade player = injector.getInstance(PlayerFacade.class);
        MusicLibrary musicLibrary = injector.getInstance(MusicLibrary.class);
        TracksLibrary tracksLibrary = injector.getInstance(TracksLibrary.class);
        injector.injectMembers(player);
        injector.injectMembers(musicLibrary);
        injector.injectMembers(tracksLibrary);
        injector.injectMembers(taskDemon);
        Stream.of(modules).forEach(m -> injector.injectMembers(m.providesController()));
    }
}