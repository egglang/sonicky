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

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.egglang.sonicky.ecc.EccDecoder;
import com.egglang.sonicky.ecc.EccInstanceProvider;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static com.egglang.sonicky.codec.SettingValues.BASELINE;
import static com.egglang.sonicky.codec.SettingValues.BIT_DURATION;
import static com.egglang.sonicky.codec.SettingValues.CHAR_FREQ;
import static com.egglang.sonicky.codec.SettingValues.CHAR_THRESH;
import static com.egglang.sonicky.codec.SettingValues.CHUNK_SIZE;
import static com.egglang.sonicky.codec.SettingValues.IDLE_LIMIT;
import static com.egglang.sonicky.codec.SettingValues.RATE;

public class Decoder {
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIOBUF_SIZE =
            AudioRecord.getMinBufferSize(
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AUDIO_FORMAT);

    private static final double[] WINDOW;

    static {
        WINDOW = new double[CHUNK_SIZE];
        for (int i = 0; i < CHUNK_SIZE; i++) { // hamming
            WINDOW[i] = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (CHUNK_SIZE - 1));
        }
    }

    private int mWinLen;
    private int mWinFudge;
    private int mBufLen;
    private int mIdleCount;
    private Deque<Integer> mBuffer;
    private Deque<Integer> mBytes;
    private byte[] mReceivedBytes = new byte[]{};
    private double[] mAudio;
    private boolean mFinished;
    private AudioRecord mAudioRec = null;
    private EccDecoder mEccDecoder;

    public Decoder(boolean eccEnabled) {
        init_(eccEnabled);
    }

    private void init_(boolean eccEnabled) {
        mWinLen = (int) (BIT_DURATION * RATE / CHUNK_SIZE);
        mWinFudge = mWinLen / 2;
        mBuffer = new ArrayDeque<>();
        mBytes = new ArrayDeque<>();
        mBufLen = mWinLen + mWinFudge;
        mIdleCount = 0;
        mEccDecoder = EccInstanceProvider.getDecoder(eccEnabled);

        Log.d(getClass().getName(), String.format("AUDIOBUF_SIZE: %d", AUDIOBUF_SIZE));
        Log.d(getClass().getName(), String.format("CHUNK_SIZE: %d", CHUNK_SIZE));
        Log.d(getClass().getName(), String.format("mWinLen: %d", mWinLen));
        Log.d(getClass().getName(), String.format("mWinFudge: %d", mWinFudge));
        Log.d(getClass().getName(), String.format("mBufLen: %d", mBufLen));

        setupAudioRecord();
    }

    private void setupAudioRecord() {
        mAudio = new double[CHUNK_SIZE];

        mFinished = false;

        mAudioRec = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RATE, AudioFormat.CHANNEL_IN_MONO,
                AUDIO_FORMAT,
                AUDIOBUF_SIZE);
        mAudioRec.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                //
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                short[] audioData = new short[CHUNK_SIZE];
                int size = recorder.read(audioData, 0, CHUNK_SIZE);
                window(audioData);

                double[] powerlist = new double[18];
                for (int i = 0; i < powerlist.length; i++) {
                    powerlist[i] = goertzel(CHAR_FREQ[i]);
                }
                double base = goertzel(BASELINE);

                updateState(powerlist, base);
                signalToBits();
                processByte();
            }
        });
        mAudioRec.setPositionNotificationPeriod(CHUNK_SIZE);
    }

    public byte[] listen() {
        mReceivedBytes = new byte[]{};
        mFinished = false;

        if (mAudioRec.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRec.startRecording();
        }

        while (true) {
            if (mFinished) break;

//            short[] audioData = readAudioData();
//            window(audioData);
//
//            int[] powerlist = new int[18];
//            for (int i = 0; i < powerlist.length; i++) {
//                powerlist[i] = goertzel(CHAR_FREQ[i]);
//            }
//            int base = goertzel(BASELINE);
//
//            updateState(powerlist, base);
//            signalToBits();
//            processByte();
        }
        return mReceivedBytes;
    }

    public void stop() {
        mFinished = true;
    }

    public void quit() {
        mFinished = true;
        mAudioRec.stop();
    }

    private short[] readAudioData() {
        short[] buffer = new short[CHUNK_SIZE];
        mAudioRec.read(buffer, 0, CHUNK_SIZE);
        return buffer;
    }

    private void window(short[] audioDatas) {
        for (int i = 0; i < WINDOW.length; i++) {
            double audio = (double) audioDatas[i];
            double window = WINDOW[i];
            mAudio[i] = audio * window;
        }
    }

    private double goertzel(int frequency) {
        double prev1 = 0.0;
        double prev2 = 0.0;
        double normFreq = frequency / (double) RATE;
        double coeff = 2 * Math.cos(2 * Math.PI * normFreq);
        double s;
        double power;

        for (double sample : mAudio) {
            s = sample + (coeff * prev1) - prev2;
            prev2 = prev1;
            prev1 = s;
        }
        power = (prev2 * prev2) + (prev1 * prev1) - (coeff * prev1 * prev2);
        return (power) + 1;
    }

    private void updateState(double[] powerlist, double base) {
        int state = -3; // silent
        final int arrayLength = powerlist.length;

        double[] pw = new double[powerlist.length];
        int[] judge = new int[arrayLength];

        double pwMax = 0;
        int sum = 0;

        for (int i = 0; i < arrayLength; i++) {
            pw[i] = powerlist[i] / base;
            judge[i] = pw[i] > CHAR_THRESH[i] ? 1 : 0;
            if (judge[i] == 0) {
                pw[i] = 0;
            }
            sum += judge[i];
        }

        if (sum > 0) {
            int maxIndex = 0;
            for (int i = 0; i < arrayLength; i++) {
                if (pwMax < pw[i]) {
                    pwMax = pw[i];
                    maxIndex = i;
                }
            }
            state = maxIndex - 2;
        }

        if (mBuffer.size() >= mBufLen) {
            mBuffer.pollFirst();

        }

        mBuffer.add(state);
    }

    private void signalToBits() {
        if (mBuffer.size() < mBufLen) {
            return;
        }

        List<Integer> buf = new ArrayList<>();
        for (int db : mBuffer) {
            buf.add(db);
        }

        final int SIGNALS_COUNT = CHAR_FREQ.length + 1;
        Costs costs = new Costs(SIGNALS_COUNT);

        for (int i = 0; i < mWinFudge; i++) {
            List<Integer> win = new ArrayList<Integer>(mWinLen);
            for (int j = i; j < (mWinLen + i); j++) win.add(buf.get(j));
            for (int k = 0; k < SIGNALS_COUNT; k++) {
                costs.append(k, sum(k, win));
            }
        }

        int signal = costs.getSignal();
        int fudge = costs.getFudge();
        for (int j = 0; j < (mWinLen + fudge); j++) {
            mBuffer.pollFirst();
        }

        // If we got a signal, put it in the byte!
        if (signal < 16) {
            mBytes.add(signal);
            Log.d("Signal:",
                    String.format("s: %s , data: %s, receive data: %s",
                            signal,
                            Arrays.toString(mBytes.toArray()),
                            Arrays.toString(mReceivedBytes))
            );
        }
        // If we get a charstart signal, reset byte!
        else if (signal == 16) {
            mBytes.clear();
        }
        // If we get a charend signal, reset byte and mBuffer!
        else if (signal == 17) {
            mBytes.clear();
            mBuffer.clear();
            stop();
        }
        // If we get no signal, increment idlecount if we are idling
        if (signal == 18) {
            mIdleCount++;
        } else {
            mIdleCount = 0;
        }
        if (IDLE_LIMIT <= mIdleCount) {
            mIdleCount = 0;
            mBytes.clear();
            clearReceivedBytes();
        }
    }

    private void clearReceivedBytes() {
        mReceivedBytes = new byte[]{};
    }

    private void processByte() {
        int len = mBytes.size();
        if (len != mEccDecoder.getLen()) {
            return;
        }
        Integer[] bytes = mBytes.toArray(new Integer[0]);
        int[] resultBytes = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            resultBytes[i / 2] = (bytes[i] << 4) + bytes[i + 1];
        }
        byte[] newByte = mEccDecoder.decode(resultBytes);
        ByteBuffer tmpbuf = ByteBuffer.allocate(mReceivedBytes.length + newByte.length);
        tmpbuf.put(mReceivedBytes);
        tmpbuf.put(newByte);
        mReceivedBytes = tmpbuf.array();
        mBytes.clear();
    }

    private int sum(int base, Collection<Integer> win) {
        int result = 0;
        if (base == 16) {
            base = -1;
        }
        if (base == 17) {
            base = -2;
        }
        if (base == 18) {
            base = -3;
        }
        for (Integer value : win) {
            if (value != base) {
                result += 1;
            }
        }
        return result;
    }

    static class Costs {
        List<Integer>[] costs;

        Costs(int size) {
            initialize(size);
        }

        private void initialize(int size) {
            costs = new ArrayList[size];
            for (int i = 0; i < size; i++) {
                costs[i] = new ArrayList<>();
            }
        }

        private void append(int index, int v) {
            costs[index].add(v);
        }

        private int[] minCosts() {
            int[] minCosts = new int[costs.length];
            for (int i = 0; i < costs.length; i++) {
                minCosts[i] = min(costs[i]);
            }
            return minCosts;
        }

        private int minCost() {
            int[] minCosts = minCosts();
            int minCost = Integer.MAX_VALUE;
            for (int minCost1 : minCosts) {
                if (minCost1 < minCost) {
                    minCost = minCost1;
                }
            }
            return minCost;
        }

        private int min(List<Integer> c) {
            int min = 65536;
            for (Integer i : c) {
                if (i < min) {
                    min = i;
                }
            }
            return min;
        }

        private int getFudge() {
            int minCost = minCost();
            // getSignal
            int signal = getSignal(); // FIXME redundant
            // getFudge
            return costs[signal].indexOf(minCost);
        }

        private int getSignal() {
            int[] minCosts = minCosts();
            int minCost = Integer.MAX_VALUE;
            int signal = 0;
            for (int i = 0; i < minCosts.length; i++) {
                if (minCosts[i] < minCost) {
                    minCost = minCosts[i];
                    signal = i;
                }
            }
            return signal;
        }
    }
}
