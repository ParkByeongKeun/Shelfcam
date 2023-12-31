/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.camctrl.bluetooth;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String RADIO_ACTIVITY_MEASUREMENT = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String MDOSE_CHARACTERISTIC_READ_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String MDOSE_CHARACTERISTIC_NOTI = "0000fff4-0000-1000-8000-00805f9b34fb";

    static {
        // RADOne Services.
        attributes.put(RADIO_ACTIVITY_MEASUREMENT, "Radioactivity Measurement (RADone)");
        // RADOne Services.
        attributes.put(MDOSE_CHARACTERISTIC_READ_WRITE, "RADone read/write");
        attributes.put(MDOSE_CHARACTERISTIC_NOTI, "RADone Notification");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
