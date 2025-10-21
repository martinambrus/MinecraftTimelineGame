package com.minecrafttimeline.headless;

import com.badlogic.gdx.graphics.GL20;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Minimal mock GL20 implementation for headless tests.
 */
public final class HeadlessMockGL20 implements GL20 {
    @Override
    public void glActiveTexture(int texture) {
        // no-op
    }

    @Override
    public void glBindTexture(int target, int texture) {
        // no-op
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor) {
        // no-op
    }

    @Override
    public void glClear(int mask) {
        // no-op
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        // no-op
    }

    @Override
    public void glClearDepthf(float depth) {
        // no-op
    }

    @Override
    public void glClearStencil(int s) {
        // no-op
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        // no-op
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
        // no-op
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
        // no-op
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
        // no-op
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        // no-op
    }

    @Override
    public void glCullFace(int mode) {
        // no-op
    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures) {
        // no-op
    }

    @Override
    public void glDeleteTexture(int texture) {
        // no-op
    }

    @Override
    public void glDepthFunc(int func) {
        // no-op
    }

    @Override
    public void glDepthMask(boolean flag) {
        // no-op
    }

    @Override
    public void glDepthRangef(float zNear, float zFar) {
        // no-op
    }

    @Override
    public void glDisable(int cap) {
        // no-op
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        // no-op
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        // no-op
    }

    @Override
    public void glEnable(int cap) {
        // no-op
    }

    @Override
    public void glFinish() {
        // no-op
    }

    @Override
    public void glFlush() {
        // no-op
    }

    @Override
    public void glFrontFace(int mode) {
        // no-op
    }

    @Override
    public void glGenTextures(int n, IntBuffer textures) {
        // no-op
    }

    @Override
    public int glGenTexture() {
        return 0;
    }

    @Override
    public int glGetError() {
        return 0;
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public String glGetString(int name) {
        return null;
    }

    @Override
    public void glHint(int target, int mode) {
        // no-op
    }

    @Override
    public void glLineWidth(float width) {
        // no-op
    }

    @Override
    public void glPixelStorei(int pname, int param) {
        // no-op
    }

    @Override
    public void glPolygonOffset(float factor, float units) {
        // no-op
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        // no-op
    }

    @Override
    public void glScissor(int x, int y, int width, int height) {
        // no-op
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask) {
        // no-op
    }

    @Override
    public void glStencilMask(int mask) {
        // no-op
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {
        // no-op
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        // no-op
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        // no-op
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        // no-op
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        // no-op
    }

    @Override
    public void glAttachShader(int program, int shader) {
        // no-op
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name) {
        // no-op
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        // no-op
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        // no-op
    }

    @Override
    public void glBindRenderbuffer(int target, int renderbuffer) {
        // no-op
    }

    @Override
    public void glBlendColor(float red, float green, float blue, float alpha) {
        // no-op
    }

    @Override
    public void glBlendEquation(int mode) {
        // no-op
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        // no-op
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        // no-op
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage) {
        // no-op
    }

    @Override
    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        // no-op
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return 0;
    }

    @Override
    public void glCompileShader(int shader) {
        // no-op
    }

    @Override
    public int glCreateProgram() {
        return 0;
    }

    @Override
    public int glCreateShader(int type) {
        return 0;
    }

    @Override
    public void glDeleteBuffer(int buffer) {
        // no-op
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers) {
        // no-op
    }

    @Override
    public void glDeleteFramebuffer(int framebuffer) {
        // no-op
    }

    @Override
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
        // no-op
    }

    @Override
    public void glDeleteProgram(int program) {
        // no-op
    }

    @Override
    public void glDeleteRenderbuffer(int renderbuffer) {
        // no-op
    }

    @Override
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
        // no-op
    }

    @Override
    public void glDeleteShader(int shader) {
        // no-op
    }

    @Override
    public void glDetachShader(int program, int shader) {
        // no-op
    }

    @Override
    public void glDisableVertexAttribArray(int index) {
        // no-op
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int indices) {
        // no-op
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        // no-op
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        // no-op
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        // no-op
    }

    @Override
    public int glGenBuffer() {
        return 0;
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers) {
        // no-op
    }

    @Override
    public void glGenerateMipmap(int target) {
        // no-op
    }

    @Override
    public int glGenFramebuffer() {
        return 0;
    }

    @Override
    public void glGenFramebuffers(int n, IntBuffer framebuffers) {
        // no-op
    }

    @Override
    public int glGenRenderbuffer() {
        return 0;
    }

    @Override
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
        // no-op
    }

    @Override
    public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
        return null;
    }

    @Override
    public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
        return null;
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {
        // no-op
    }

    @Override
    public int glGetAttribLocation(int program, String name) {
        return 0;
    }

    @Override
    public void glGetBooleanv(int pname, Buffer params) {
        // no-op
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params) {
        // no-op
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glGetProgramiv(int program, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public String glGetProgramInfoLog(int program) {
        return null;
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public String glGetShaderInfoLog(int shader) {
        return null;
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
        // no-op
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        // no-op
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glGetUniformfv(int program, int location, FloatBuffer params) {
        // no-op
    }

    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params) {
        // no-op
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return 0;
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
        // no-op
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
        // no-op
    }

    @Override
    public boolean glIsBuffer(int buffer) {
        return false;
    }

    @Override
    public boolean glIsEnabled(int cap) {
        return false;
    }

    @Override
    public boolean glIsFramebuffer(int framebuffer) {
        return false;
    }

    @Override
    public boolean glIsProgram(int program) {
        return false;
    }

    @Override
    public boolean glIsRenderbuffer(int renderbuffer) {
        return false;
    }

    @Override
    public boolean glIsShader(int shader) {
        return false;
    }

    @Override
    public boolean glIsTexture(int texture) {
        return false;
    }

    @Override
    public void glLinkProgram(int program) {
        // no-op
    }

    @Override
    public void glReleaseShaderCompiler() {
        // no-op
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        // no-op
    }

    @Override
    public void glSampleCoverage(float value, boolean invert) {
        // no-op
    }

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
        // no-op
    }

    @Override
    public void glShaderSource(int shader, String string) {
        // no-op
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        // no-op
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask) {
        // no-op
    }

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
        // no-op
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        // no-op
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        // no-op
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        // no-op
    }

    @Override
    public void glUniform1f(int location, float x) {
        // no-op
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v) {
        // no-op
    }

    @Override
    public void glUniform1fv(int location, int count, float v[], int offset) {
        // no-op
    }

    @Override
    public void glUniform1i(int location, int x) {
        // no-op
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v) {
        // no-op
    }

    @Override
    public void glUniform1iv(int location, int count, int v[], int offset) {
        // no-op
    }

    @Override
    public void glUniform2f(int location, float x, float y) {
        // no-op
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v) {
        // no-op
    }

    @Override
    public void glUniform2fv(int location, int count, float v[], int offset) {
        // no-op
    }

    @Override
    public void glUniform2i(int location, int x, int y) {
        // no-op
    }

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v) {
        // no-op
    }

    @Override
    public void glUniform2iv(int location, int count, int[] v, int offset) {
        // no-op
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z) {
        // no-op
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v) {
        // no-op
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset) {
        // no-op
    }

    @Override
    public void glUniform3i(int location, int x, int y, int z) {
        // no-op
    }

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v) {
        // no-op
    }

    @Override
    public void glUniform3iv(int location, int count, int v[], int offset) {
        // no-op
    }

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w) {
        // no-op
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v) {
        // no-op
    }

    @Override
    public void glUniform4fv(int location, int count, float v[], int offset) {
        // no-op
    }

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w) {
        // no-op
    }

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v) {
        // no-op
    }

    @Override
    public void glUniform4iv(int location, int count, int v[], int offset) {
        // no-op
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
        // no-op
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float value[], int offset) {
        // no-op
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
        // no-op
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float value[], int offset) {
        // no-op
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
        // no-op
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float value[], int offset) {
        // no-op
    }

    @Override
    public void glUseProgram(int program) {
        // no-op
    }

    @Override
    public void glValidateProgram(int program) {
        // no-op
    }

    @Override
    public void glVertexAttrib1f(int indx, float x) {
        // no-op
    }

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values) {
        // no-op
    }

    @Override
    public void glVertexAttrib2f(int indx, float x, float y) {
        // no-op
    }

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values) {
        // no-op
    }

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z) {
        // no-op
    }

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values) {
        // no-op
    }

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
        // no-op
    }

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values) {
        // no-op
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
        // no-op
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
        // no-op
    }

}
