/*
 * Copyright 2014-2017 Real Logic Ltd.
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
package io.aeron.archiver;

import io.aeron.Publication;
import io.aeron.archiver.codecs.*;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.concurrent.IdleStrategy;

class RecordingEventsProxy
{
    private final IdleStrategy idleStrategy;
    private final Publication recordingEventsPublication;
    private final ExpandableDirectByteBuffer outboundBuffer = new ExpandableDirectByteBuffer(512);
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final RecordingStartedEncoder recordingStartedEncoder = new RecordingStartedEncoder();
    private final RecordingProgressEncoder recordingProgressEncoder = new RecordingProgressEncoder();
    private final RecordingStoppedEncoder recordingStoppedEncoder = new RecordingStoppedEncoder();

    RecordingEventsProxy(final IdleStrategy idleStrategy, final Publication recordingEventsPublication)
    {
        this.idleStrategy = idleStrategy;
        this.recordingEventsPublication = recordingEventsPublication;
    }

    void started(
        final long recordingId,
        final long joinPosition,
        final int sessionId,
        final int streamId,
        final String channel,
        final String sourceIdentity)
    {
        recordingStartedEncoder
            .wrapAndApplyHeader(outboundBuffer, 0, messageHeaderEncoder)
            .recordingId(recordingId)
            .joinPosition(joinPosition)
            .sessionId(sessionId)
            .streamId(streamId)
            .channel(channel)
            .sourceIdentity(sourceIdentity);

        send(recordingStartedEncoder.encodedLength());
    }

    void progress(final long recordingId, final long joinPosition, final long position)
    {
        recordingProgressEncoder
            .wrapAndApplyHeader(outboundBuffer, 0, messageHeaderEncoder)
            .recordingId(recordingId)
            .joinPosition(joinPosition)
            .position(position);

        send(recordingProgressEncoder.encodedLength());
    }

    void stopped(final long recordingId, final long joinPosition, final long endPosition)
    {
        recordingStoppedEncoder
            .wrapAndApplyHeader(outboundBuffer, 0, messageHeaderEncoder)
            .recordingId(recordingId)
            .joinPosition(joinPosition)
            .endPosition(endPosition);

        send(recordingStoppedEncoder.encodedLength());
    }

    private void send(final int length)
    {
        final int fullLength = MessageHeaderEncoder.ENCODED_LENGTH + length;
        while (true)
        {
            // TODO: should we make this publication unreliable?
            // TODO: Under back pressure it should drop sends and then do an update on timeout to avoid tail loss.
            final long result = recordingEventsPublication.offer(outboundBuffer, 0, fullLength);
            if (result > 0 || result == Publication.NOT_CONNECTED)
            {
                idleStrategy.reset();
                break;
            }

            if (result == Publication.CLOSED || result == Publication.MAX_POSITION_EXCEEDED)
            {
                throw new IllegalStateException();
            }

            idleStrategy.idle();
        }
    }
}
