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

package com.transgressoft.musicott.services.lastfm;

import javax.xml.bind.annotation.*;

/**
 * @author Octavio Calleya
 * @version 0.10-b
 */
@XmlRootElement (name = "lfm")
public class LastFmResponse {

    private String token;
    private String status;
    private LastFmError error;
    private LastFmSession session;

    @XmlAttribute
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlElement
    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @XmlElement
    public LastFmError getError() {
        return this.error;
    }

    public void setError(LastFmError error) {
        this.error = error;
    }

    @XmlElement
    public LastFmSession getSession() {
        return session;
    }

    public void setSession(LastFmSession session) {
        this.session = session;
    }
}
