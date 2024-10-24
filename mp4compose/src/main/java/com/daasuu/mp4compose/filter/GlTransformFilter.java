package com.daasuu.mp4compose.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;


public class GlTransformFilter extends GlFilter {

    /*
        OpenGL Matrices are stored data in column-major order.
     */

    public static final String TRANSFORM_VERTEX_SHADER =
                    "   attribute highp vec4 aPosition;\n" +
                    "   attribute highp vec4 aTextureCoord;\n" +
                    "   varying highp vec2 vTextureCoord;\n" +
                    "   uniform mat4 orthographicMatrix;\n" +
                    "   varying vec2 textureCoordinate;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     gl_Position = vec4(aPosition.xyz, 1.0) * orthographicMatrix;\n" +
                    "     vTextureCoord = aTextureCoord.xy;\n" +
                    " }";

    public static final String FRAGMENT_SHADER ="" +
            "   precision highp float;\n" +
            "   varying highp vec2 vTextureCoord;\n" +
            "   uniform lowp sampler2D sTexture;\n" +
            "   uniform mat4 transformMatrix;\n" +
            "void main() {" +
            "   vec4 uv = vec4(vTextureCoord.xy,1.0,0.0);" +
            "   uv -= vec4(0.5,0.5,0.0,0.0);" +
            "   uv =  transformMatrix * uv;" +
            "   uv += vec4(0.5,0.5,0.0,0.0);" +
            "   gl_FragColor = texture2D(sTexture, uv.xy);\n" +
            "}\n";


    private float[] orthographicM;
    private float[] transform3D;
    private FloatBuffer modifiedBuffer;

    private float[] rotationM;
    private float[] scaleM;
    private float[] translateM;
    private float angle;
    private float aspectRatio;
    private int width;
    private int height;

    public GlTransformFilter()
    {
        super(TRANSFORM_VERTEX_SHADER,FRAGMENT_SHADER);

        orthographicM = new float[16];
        Matrix.orthoM(orthographicM,0,-1f,1f,-1f,1f,-1.0f,1f);
        aspectRatio = 1080f / 1920f;
        transform3D = new float[16];

        Matrix.setIdentityM(transform3D,0);

    }

    @Override
    public void setup() {
        super.setup();
        getHandle("transformMatrix");
        getHandle("orthographicMatrix");
    }

    public void resetTrasformation() {
        Matrix.setIdentityM(transform3D,0);
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);

        this.height = height;
        this.width = width;
        aspectRatio = (float) width/ (float) height;
        //Matrix.orthoM(orthographicM,0,-1.0f,1.0f, -1.0f , 1.0f * (float) height/ (float) width,-1f,1f);
    }

    @Override
    protected void onDraw() {

        GLES20.glUniformMatrix4fv(getHandle("transformMatrix"), 1, false, transform3D, 0);
        GLES20.glUniformMatrix4fv(getHandle("orthographicMatrix"), 1, false, orthographicM, 0);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);


    }

    public void setTransform3D(float[] transform3D) {
        this.transform3D = transform3D;
    }


    public void setTranslateOffset(float offsetX, float offsetY){

        translateM = new float[16];
        Matrix.setIdentityM(translateM,0);
        //store data in column major order.
        translateM[8] = -offsetX;
        translateM[9] = -offsetY;
        Matrix.multiplyMM(transform3D, 0, transform3D, 0, translateM, 0);

    }

    public void setRotateInAngle(float angleInDegree)
    {
        if(getOutputHeight()>0 && getOutputWidth() > 0)
        {
            aspectRatio = (float) getOutputWidth()/ (float) getOutputHeight();
        }

        float[]  rotationM = new float[16];
        Matrix.setIdentityM(rotationM,0);

        double radAngle = Math.toRadians(angleInDegree);

        rotationM[0] = (float) Math.cos(radAngle);
        rotationM[1] = (float) Math.sin(radAngle);
        rotationM[4] = (float) Math.sin(radAngle) * -1.0f;
        rotationM[5] = (float) Math.cos(radAngle);

        float[]  scaleMat = new float[16];
        Matrix.setIdentityM(scaleMat,0);
        scaleMat[0] = aspectRatio;

        float[]  scaleMatInv = new float[16];
        Matrix.setIdentityM(scaleMatInv,0);
        scaleMatInv[0] = 1f/aspectRatio;

        Matrix.multiplyMM(rotationM, 0, scaleMatInv, 0, rotationM , 0);

        Matrix.multiplyMM(rotationM, 0, rotationM, 0, scaleMat, 0);

        Matrix.multiplyMM(transform3D, 0, transform3D, 0, rotationM, 0);

    }


    public void setScaleUnit(float scaleX,float scaleY)
    {
        scaleM = new float[16];
        Matrix.setIdentityM(scaleM,0);
        scaleM[0] = scaleX;
        scaleM[5] = scaleY;
        Matrix.multiplyMM(transform3D, 0, transform3D, 0, scaleM, 0);
    }

}
