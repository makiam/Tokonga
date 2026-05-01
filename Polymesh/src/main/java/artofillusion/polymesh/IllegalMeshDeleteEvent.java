/*
   Copyright (C) 2025-2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

public record IllegalMeshDeleteEvent(PolyMesh mesh) {
    void fire() {
        org.greenrobot.eventbus.EventBus.getDefault().post(this);
    }

    public PolyMesh getMesh() {
        return mesh;
    }
}
