package com.minecrafttimeline.core.testing;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility helper to ensure libGDX native libraries are loaded once before tests execute.
 */
public final class GdxNativeTestUtils {

    private static final AtomicBoolean LOADED = new AtomicBoolean();
    private static final AtomicBoolean HEADLESS_INITIALIZED = new AtomicBoolean();
    private static HeadlessApplication headlessApplication;

    private GdxNativeTestUtils() {
        // utility class
    }

    /**
     * Loads the libGDX native libraries exactly once for the current JVM.
     */
    public static void loadNativesIfNeeded() {
        if (LOADED.compareAndSet(false, true)) {
            GdxNativesLoader.load();
        }
    }

    /**
     * Ensures a libGDX headless application is running so static {@code Gdx} services are available.
     * This is primarily required for tests that rely on {@code Gdx.graphics} during viewport updates.
     */
    public static void ensureHeadlessApplication() {
        loadNativesIfNeeded();
        if (HEADLESS_INITIALIZED.compareAndSet(false, true)) {
            final HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
            headlessApplication = new HeadlessApplication(new ApplicationAdapter() {}, configuration);
            final GL20 mockGl = (GL20) Proxy.newProxyInstance(
                    GL20.class.getClassLoader(),
                    new Class<?>[]{GL20.class},
                    new HeadlessGlInvocationHandler());
            Gdx.gl = mockGl;
            Gdx.gl20 = mockGl;
        }
    }

    private static final class HeadlessGlInvocationHandler implements InvocationHandler {

        private final AtomicInteger idCounter = new AtomicInteger(1);

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            final Class<?> returnType = method.getReturnType();
            final String name = method.getName();

            if ("glGetShaderiv".equals(name) && args != null && args.length >= 3) {
                final int pname = toInt(args[1]);
                if (pname == GL20.GL_COMPILE_STATUS) {
                    writeInt(args[2], GL20.GL_TRUE);
                } else {
                    writeInt(args[2], 0);
                }
                return null;
            }
            if ("glGetProgramiv".equals(name) && args != null && args.length >= 3) {
                final int pname = toInt(args[1]);
                if (pname == GL20.GL_LINK_STATUS) {
                    writeInt(args[2], GL20.GL_TRUE);
                } else if (pname == GL20.GL_ACTIVE_ATTRIBUTES || pname == GL20.GL_ACTIVE_UNIFORMS) {
                    writeInt(args[2], 0);
                } else {
                    writeInt(args[2], 0);
                }
                return null;
            }
            if ("glGetShaderInfoLog".equals(name) || "glGetProgramInfoLog".equals(name)) {
                return "";
            }
            if ("glGetError".equals(name)) {
                return GL20.GL_NO_ERROR;
            }
            if ("glCheckFramebufferStatus".equals(name)) {
                return GL20.GL_FRAMEBUFFER_COMPLETE;
            }
            if ("glCreateProgram".equals(name) || "glCreateShader".equals(name)) {
                return idCounter.getAndIncrement();
            }
            if (name != null && name.startsWith("glGen") && args != null && args.length > 0) {
                fillBuffer(args[args.length - 1]);
                return null;
            }

            if (returnType.equals(Boolean.TYPE)) {
                return Boolean.FALSE;
            }
            if (returnType.equals(Integer.TYPE)) {
                if ("glGetUniformLocation".equals(name)) {
                    return 0;
                }
                return 0;
            }
            if (returnType.equals(Long.TYPE)) {
                return 0L;
            }
            if (returnType.equals(Float.TYPE)) {
                return 0f;
            }
            if (returnType.equals(Double.TYPE)) {
                return 0d;
            }
            if (returnType.equals(Short.TYPE)) {
                return (short) 0;
            }
            if (returnType.equals(Byte.TYPE)) {
                return (byte) 0;
            }
            if (returnType.equals(Character.TYPE)) {
                return (char) 0;
            }
            return null;
        }

        private int toInt(final Object value) {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return 0;
        }

        private void fillBuffer(final Object buffer) {
            if (buffer instanceof IntBuffer) {
                final IntBuffer intBuffer = (IntBuffer) buffer;
                final int limit = intBuffer.limit();
                for (int i = 0; i < limit; i++) {
                    intBuffer.put(i, idCounter.getAndIncrement());
                }
            } else if (buffer instanceof FloatBuffer) {
                final FloatBuffer floatBuffer = (FloatBuffer) buffer;
                final int limit = floatBuffer.limit();
                for (int i = 0; i < limit; i++) {
                    floatBuffer.put(i, 0f);
                }
            }
        }

        private void writeInt(final Object buffer, final int value) {
            if (buffer instanceof IntBuffer) {
                final IntBuffer intBuffer = (IntBuffer) buffer;
                final int position = intBuffer.position();
                if (position < intBuffer.limit()) {
                    intBuffer.put(position, value);
                } else if (intBuffer.limit() > 0) {
                    intBuffer.put(0, value);
                }
            } else if (buffer instanceof int[]) {
                final int[] array = (int[]) buffer;
                if (array.length > 0) {
                    array[0] = value;
                }
            }
        }
    }
}
