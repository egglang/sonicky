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
import android.media.AudioManager;
import android.media.AudioTrack;

import com.egglang.sonicky.ecc.EccEncoder;
import com.egglang.sonicky.ecc.EccInstanceProvider;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.egglang.sonicky.codec.SettingValues.RATE;

public class Encoder {
    private EccEncoder mEccEncoder;
    private AudioTrack mAudioTrack;
    private ExecutorService mExecutorService;

    class PlaySoundTask implements Runnable {
        private final byte[] mArrayToSend;

        public PlaySoundTask(byte[] arrayToSend) {
            mArrayToSend = arrayToSend;
        }

        @Override
        public void run() {
            mAudioTrack.play();
            mAudioTrack.write(mArrayToSend, 0, mArrayToSend.length);
            mAudioTrack.stop();
        }
    }

    public Encoder(boolean eccEnabled) {
        init_(eccEnabled);
    }

    private void init_(boolean eccEnabled) {
        mEccEncoder = EccInstanceProvider.getEncoder(eccEnabled);
        int minBufferSizeInBytes = AudioTrack.getMinBufferSize(
                RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // 44.1kHz mono 16bit
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSizeInBytes,
                AudioTrack.MODE_STREAM);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public void encodePlay(byte[] byteData) {
        double[] soundList = string2sound(byteData);
        // convert double to byte
        short[] arrayToSend = new short[soundList.length];
        for (int i = 0; i < soundList.length; i++) {
            arrayToSend[i] = (short) soundList[i];
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (short s : arrayToSend) {
            stream.write(s & 0xff);
            stream.write((s >> 8) & 0xff);
        }
        byte[] byteArrayToSend = stream.toByteArray();
        mExecutorService.execute(new PlaySoundTask(byteArrayToSend));
    }

    public void quit() {
        mAudioTrack.release();
    }

    private double[] string2sound(byte[] byteData) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte b : byteData) {
            bytes.write(-1); // -001
            byte[] encodedBytes = resolveEncodedBytes(b);
            for (byte encoded : encodedBytes) {
                bytes.write(encoded);
            }
        }
        bytes.write(-2);
        byte[] multiple = bytes.toByteArray();
        List<Double> soundList = new ArrayList<>();
        for (int i = 0; i < multiple.length; i++) {
            double[] gotten = getBit(SettingValues.CHAR_FREQ[multiple[i] + 2]);
            for (double g : gotten) {
                soundList.add(g);
            }
        }
        double[] result = new double[soundList.size()];
        for (int i = 0; i < soundList.size(); i++) {
            result[i] = soundList.get(i);
        }
        return result;
    }

    private byte[] resolveEncodedBytes(byte b) {
        byte[] encodedBytes = mEccEncoder.getEncodedBytes(b);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte encodedByte : encodedBytes) {
            byte b1 = (byte) ((encodedByte & 0xF0) >>> 4 & 0x0F);
            byte b2 = (byte) (encodedByte & 0x0F);
            bytes.write(b1);
            bytes.write(b2);
        }
        return bytes.toByteArray();
    }

    private double[] linspace(double min, double max, double step) {
        List<Double> d = new ArrayList<>();
        double current = min + step;
        while (current < max) {
            d.add(current);
            current += step;
        }
        double[] result = new double[d.size()];
        for (int i = 0; i < d.size(); i++) {
            result[i] = d.get(i);
        }
        return result;
    }

    private double[] createSin(double freq, double[] time) {
        int length = time.length;
        double[] data = new double[length];
        for (int i = 0; i < length; i++) {
            data[i] = (int) ((double) 32000 * Math.sin(2 * Math.PI * freq * time[i]));
        }
        return data;
    }

    private double sigmoid(double x) {
        return ((double) 1 / ((double) 1 + Math.pow(Math.E, (-1 * x))));
    }

    private double[] getBit(double freq) {
        double[] t = linspace(0, SettingValues.BIT_DURATION, (double) 1 / (double) RATE);
        double[] x = createSin(freq, t);
        double[] b = linspace(-6, 6, 0.02);
        int length = b.length;
        double[] sigmoid = new double[length];
        for (int i = 0; i < length; i++) {
            sigmoid[i] = sigmoid(b[i]);
        }
        double[] sigmoidInv = new double[length];
        for (int i = 0; i < length; i++) {
            sigmoidInv[length - i - 1] = sigmoid[i];
        }
        int xstart = x.length - length;
        for (int i = 0; i < length; i++) {
            x[xstart + i] *= sigmoidInv[i];
            x[i] *= sigmoid[i];
        }
        return x;
    }
}
