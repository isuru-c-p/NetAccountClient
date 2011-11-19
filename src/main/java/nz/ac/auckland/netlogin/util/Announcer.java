/*
Copyright (c) 2000-2007, jMock.org
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of
conditions and the following disclaimer. Redistributions in binary form must reproduce
the above copyright notice, this list of conditions and the following disclaimer in
the documentation and/or other materials provided with the distribution.

Neither the name of jMock nor the names of its contributors may be used to endorse
or promote products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.
*/
package nz.ac.auckland.netlogin.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * From: http://nat.truemesh.com/archives/000710.html
 */
public class Announcer<T extends EventListener> {
	private final T proxy;
	private final List<T> listeners = new ArrayList<T>();


	public Announcer(Class<? extends T> listenerType) {
		proxy = listenerType.cast(Proxy.newProxyInstance(
			listenerType.getClassLoader(),
			new Class<?>[]{listenerType},
			new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					announce(method, args);
					return null;
				}
			}));
	}

	public void addListener(T listener) {
		listeners.add(listener);
	}

	public void removeListener(T listener) {
		listeners.remove(listener);
	}

	public T announce() {
		return proxy;
	}

	private void announce(Method m, Object[] args) {
		try {
			for (T listener : listeners) {
				m.invoke(listener, args);
			}
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("could not invoke listener", e);
		}
		catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			else if (cause instanceof Error) {
				throw (Error)cause;
			}
			else {
				throw new UnsupportedOperationException("listener threw exception", cause);
			}
		}
	}

	public static <T extends EventListener> Announcer<T> to(Class<? extends T> listenerType) {
		return new Announcer<T>(listenerType);
	}
}