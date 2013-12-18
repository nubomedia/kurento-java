/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.kurento.kmf.media;

import static com.kurento.kmf.media.SyncMediaServerTest.URL_POINTER_DETECOR;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.kurento.kmf.common.exception.KurentoMediaFrameworkException;
import com.kurento.kmf.media.events.EndOfStreamEvent;
import com.kurento.kmf.media.events.MediaEventListener;

/**
 * {@link FaceOverlayFilter} test suite.
 * 
 * @author Ivan Gracia (igracia@gsyc.es)
 * @since 2.0.1
 * 
 */
public class FaceOverlayFilterAsyncTest extends AbstractAsyncBaseTest {

	private PlayerEndpoint player;

	private FaceOverlayFilter overlayFilter;

	@Before
	public void setup() throws InterruptedException {
		player = pipeline.newPlayerEndpoint(URL_POINTER_DETECOR).build();

		final BlockingQueue<FaceOverlayFilter> events = new ArrayBlockingQueue<FaceOverlayFilter>(
				1);
		pipeline.newFaceOverlayFilter().buildAsync(
				new Continuation<FaceOverlayFilter>() {

					@Override
					public void onSuccess(FaceOverlayFilter result) {
						events.add(result);
					}

					@Override
					public void onError(Throwable cause) {
						throw new KurentoMediaFrameworkException(cause);
					}
				});
		overlayFilter = events.poll(4, SECONDS);
		Assert.assertNotNull(overlayFilter);
	}

	@After
	public void teardown() throws InterruptedException {
		player.release();
		releaseMediaObject(overlayFilter);
	}

	/**
	 * Test if a {@link FaceOverlayFilter} can be created in the KMS. The filter
	 * is pipelined with a {@link PlayerEndpoint}, which feeds video to the
	 * filter. This test depends on the correct behaviour of the player and its
	 * events.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testFaceOverlayFilter() throws InterruptedException {
		player.connect(overlayFilter);
		final BlockingQueue<EndOfStreamEvent> events = new ArrayBlockingQueue<EndOfStreamEvent>(
				1);
		player.addEndOfStreamListener(new MediaEventListener<EndOfStreamEvent>() {

			@Override
			public void onEvent(EndOfStreamEvent event) {
				events.add(event);
			}
		});

		player.play();

		Assert.assertNotNull(events.poll(20, SECONDS));
	}

}