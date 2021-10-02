/*
 * file: Main.java
 * author: N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Program 1
 * date last modified: 8/23/2021
 *
 * purpose: This program reads a coordinates.txt file and renders it in LWJGL.
 * It uses glVertex2f to render each shape point-by-point.
 * Pressing ESC will close the window.
 */
package cs4450_project;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.*;

public final class Main {

    public static final int HEIGHT = 480;
    public static final int WIDTH = 640;
    private final Cube cube;

    /*
     * constructor
     * purpose: initialize instance variables
     * and ensure only this class can construct itself.
     */
    private Main() {
        cube = new Cube(0, 0, 0, 1);
    }

    /*
     * method: createWindow
     * purpose: Initialize the OpenGL display
     */
    private void createWindow() throws LWJGLException {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
        Display.setTitle("CS4450 Project");
        Display.create();
    }

    /*
     * method: initOpenGL
     * purpose: Initialize the rest of OpenGL once the display is opened.
     */
    private void initOpenGL() {
        glClearColor(0, 0, 0, 0);
        glClearDepth(1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(45,
            WIDTH / (float) HEIGHT,
            0.1f,
            100);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    /*
     * method: render
     * purpose: Draw to the display until it closes
     */
    private void render() {
        while (!Display.isCloseRequested()) try {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();

            glPointSize(10);

            // TODO: Apply transforms from camera.
            glTranslatef(0, 0, -5);
            cube.draw();

            Display.update();
            Display.sync(60);
        } catch (Exception e) {
        }

        Display.destroy();
    }

    /**
     * method: start
     * purpose: start each component of the program.
     */
    private void start() {
        try {
            createWindow();
            initOpenGL();
            render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method: main
     * purpose: run the whole program
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

}
