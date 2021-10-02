/*
 * file: Cube.java
 * author: N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 10/2/2021
 *
 * purpose: This class represents 1 cube in 3d space.
 */
package cs4450_project;

import static org.lwjgl.opengl.GL11.*;

public class Cube {

    private float size, x, y, z;

    // method: constructor
    // purpose: initialize the cube
    // with the point (x, y, z) at its bottom-left corner.
    public Cube(float x, float y, float z, float size) {
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // method: draw
    // purpose: draw this cube at its location
    public void draw() {
        glBegin(GL_QUADS);
        { // Front
            glColor3f(1, 1, 0);
            glVertex3f(x, y, z);
            glVertex3f(x + size, y, z);
            glVertex3f(x + size, y + size, z);
            glVertex3f(x, y + size, z);
        }
        { // Back
            glVertex3f(x, y, z + size);
            glVertex3f(x + size, y, z + size);
            glVertex3f(x + size, y + size, z + size);
            glVertex3f(x, y + size, z + size);
        }
        { // Left
            glVertex3f(x, y, z);
            glVertex3f(x, y, z + size);
            glVertex3f(x, y + size, z + size);
            glVertex3f(x, y + size, z);
        }
        { // Right
            glVertex3f(x + size, y, z);
            glVertex3f(x + size, y, z + size);
            glVertex3f(x + size, y + size, z + size);
            glVertex3f(x + size, y + size, z);
        }
        { // Top
            glVertex3f(x, y + size, z);
            glVertex3f(x + size, y + size, z);
            glVertex3f(x + size, y + size, z + size);
            glVertex3f(x, y + size, z + size);
        }
        { // Bottom
            glVertex3f(x, y, z);
            glVertex3f(x + size, y, z);
            glVertex3f(x + size, y, z + size);
            glVertex3f(x, y, z + size);
        }
        glEnd();
    }
}
