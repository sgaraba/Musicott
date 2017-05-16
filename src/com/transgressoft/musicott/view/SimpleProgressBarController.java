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

package com.transgressoft.musicott.view;

import com.google.inject.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.*;

/**
 * Controller class of a simple progress bar
 *
 * @author Octavio Calleya
 * @version 0.10.1-b
 * @since 0.10.1-b
 */
@Singleton
public class SimpleProgressBarController extends InjectableController<AnchorPane> {

    @FXML
    private AnchorPane root;
    @FXML
    private ProgressBar progressBar;

    @Override
    public AnchorPane getRoot() {
        return root;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setTitle("");
        stage.initStyle(StageStyle.UNDECORATED);
    }
}