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

package com.transgressoft.musicott.util.guice.modules;

import com.google.inject.*;
import javafx.application.*;

/**
 * @author Octavio Calleya
 */
public class HostServicesModule extends AbstractModule {

    private HostServices hostServices;

    public HostServicesModule(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    protected void configure() {
        // Nothing to configure here, just provide the application Host Services
    }

    @Provides
    HostServices providesHostServices() {
        return hostServices;
    }
}