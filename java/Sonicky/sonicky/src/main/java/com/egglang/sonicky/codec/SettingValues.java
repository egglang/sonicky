/*
 * Copyright 2017 egglang <t.egawa@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.egglang.sonicky.codec;

public class SettingValues {
    static final int CHUNK_SIZE = 512;
    static final int IDLE_LIMIT = 2;
    static final double BIT_DURATION = 0.05;
    static final int RATE = 44100;
    static final int BASELINE = 17000;
    static final int[] CHAR_FREQ = {
            17300, // start
            17500, // end
            17750, // 0
            17900, // 1
            18050, // 2
            18200, // 3
            18350, // 4
            18500, // 5
            18650, // 6
            18800, // 7
            18950, // 8
            19100, // 9
            19250, // 10
            19400, // 11
            19550, // 12
            19700, // 13
            19850, // 14
            20000  // 15
    };

    static final int[] CHAR_THRESH = {
            20, // start
            20, // end
            20, // 0
            20, // 1
            20, // 2
            20, // 3
            20, // 4
            20, // 5
            20, // 6
            20, // 7
            20, // 8
            20, // 9
            20, // 10
            20, // 11
            20, // 12
            20, // 13
            20, // 14
            20  // 15
    };
}
