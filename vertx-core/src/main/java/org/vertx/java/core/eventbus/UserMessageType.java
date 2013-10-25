package org.vertx.java.core.eventbus;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.impl.BaseMessage;

/** Interface which must be implemented by a provider of a userMessage.
 * Implementations of this interface act as factories for new messages.
 * 
 * @author jpaw.de
 *
 * @param <U>   The message type implemented.
 */
public interface UserMessageType<U> {
    /** Returns the type of the message (typically the class name of U). */
    String getId();
    
    /** Returns the numeric messageType (the byte transmitted to remotes). */
    byte getDefaultNumericUserMessageType();
    
    /** Message factory for a new message of type U, created from a buffer. */
    BaseMessage<U> createMessage(Buffer buff);
}
