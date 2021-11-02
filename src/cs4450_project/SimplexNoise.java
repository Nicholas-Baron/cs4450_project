/*
 * file: Simplex.java
 * author: T. Diaz, I. Quintanilla, N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 10/23/2021
 *
 * purpose: This class generates and stores simplex noise.
 */
package cs4450_project;

import java.util.Random;

public class SimplexNoise {

    private final double[] amplitudes;
    private final double[] frequencys;
    private final int largestFeature;
    private final SimplexNoise_octave[] octaves;
    private final double persistence;
    private final int seed;

    // method: constructor
    // purpose: generate and store the simplex noise
    public SimplexNoise(int largestFeature, double persistence, int seed) {
        this.largestFeature = largestFeature;
        this.persistence = persistence;
        this.seed = seed;

        //recieves a number (eg 128) and calculates what power of 2 it is (eg 2^7)
        int numberOfOctaves = (int) Math.ceil(Math.log10(largestFeature)
            / Math.log10(2));

        octaves = new SimplexNoise_octave[numberOfOctaves];
        frequencys = new double[numberOfOctaves];
        amplitudes = new double[numberOfOctaves];

        Random rnd = new Random(seed);

        for (int i = 0; i < numberOfOctaves; i++) {
            octaves[i] = new SimplexNoise_octave(rnd.nextInt());

            frequencys[i] = Math.pow(2, i);
            amplitudes[i] = Math.pow(persistence, octaves.length - i);
        }
    }

    // method: getNoise
    // purpose: return the noise value at a 2D position
    public double getNoise(int x, int y) {

        double result = 0;

        for (int i = 0; i < octaves.length; i++) {
            result += octaves[i].noise(
                x / frequencys[i],
                y / frequencys[i])
                * amplitudes[i];
        }

        return result;
    }

    // method: getNoise
    // purpose: return the noise value at a 3D position
    public double getNoise(int x, int y, int z) {

        double result = 0;

        for (int i = 0; i < octaves.length; i++) {
            double frequency = Math.pow(2, i);
            double amplitude = Math.pow(persistence, octaves.length - i);

            result += octaves[i].noise(
                x / frequency,
                y / frequency,
                z / frequency)
                * amplitude;
        }

        return result;
    }
}
