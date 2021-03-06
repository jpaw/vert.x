/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.core.eventbus.impl;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.UserMessageType;
import de.jpaw.bonaparte.vertx.BonaPortableMessage;     // 1 case
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MessageFactory {

  static final byte TYPE_PING = 0;
  static final byte TYPE_BUFFER = 1;
  static final byte TYPE_BOOLEAN = 2;
  static final byte TYPE_BYTEARRAY = 3;
  static final byte TYPE_BYTE = 4;
  static final byte TYPE_CHARACTER = 5;
  static final byte TYPE_DOUBLE = 6;
  static final byte TYPE_FLOAT = 7;
  static final byte TYPE_INT = 8;
  static final byte TYPE_LONG = 9;
  static final byte TYPE_SHORT = 10;
  static final byte TYPE_STRING = 11;
  static final byte TYPE_JSON_OBJECT = 12;
  static final byte TYPE_JSON_ARRAY = 13;
  static final byte TYPE_REPLY_FAILURE = 100;

  static public final byte TYPE_BONAPORTABLE_ASCII = 80;

  static public final byte USER_MESSAGE_TYPE_FIRST = 40;
  static public final byte USER_MESSAGE_TYPE_NUM = 20;

  /** Storage for a couple of user message types, which have to register with this factory in order to be used.
   * Due to the polyglot character of the framework, there is no central instance to avoid collisions.
   * It is the responsibility of the caller to ensure that a certain message type has not yet been used.
   * This implementation will throw an error if there is an attempt to register the same message type twice.
   */
  static final private UserMessageType<?> userMessageTypes[] = new UserMessageType[USER_MESSAGE_TYPE_NUM];

  static private void rangeCheck(byte messageType) throws Exception {
      if (messageType < USER_MESSAGE_TYPE_FIRST || messageType >= USER_MESSAGE_TYPE_FIRST + USER_MESSAGE_TYPE_NUM)
          throw new Exception("userMessageType out of range (must be within ["
              + USER_MESSAGE_TYPE_FIRST + ","
              + (USER_MESSAGE_TYPE_FIRST + USER_MESSAGE_TYPE_NUM - 1) + "]");
  }
  
  static public void registerUserMessageType(final UserMessageType<?> implementation) throws Exception {
      byte messageType = implementation.getDefaultNumericUserMessageType();
      rangeCheck(messageType);
      UserMessageType<?> previous;
      synchronized (userMessageTypes) {
          previous = userMessageTypes[messageType-USER_MESSAGE_TYPE_FIRST]; 
          if (previous == null) {
              userMessageTypes[messageType-USER_MESSAGE_TYPE_FIRST] = implementation;
              return;
          }
      }
      // message type was assigned before. Ignore this if the previous assignment was the same as the new one should be.
      if (previous.getId().equals(implementation.getId()))
          return;
      // messageType was assigned before, and also to a different implementation 
      throw new Exception("Attempt to perform duplicate assignment for message type " + messageType + ": first to "
              + previous.getId() + ", now to " + implementation.getId());
  }
  
  static public UserMessageType<?> getFactory(byte messageType) throws Exception {
      rangeCheck(messageType);
      return userMessageTypes[messageType-USER_MESSAGE_TYPE_FIRST];
  }
  
  static BaseMessage<?> read(Buffer buff) {
    byte type = buff.getByte(0);
    if (type >= USER_MESSAGE_TYPE_FIRST && type < USER_MESSAGE_TYPE_FIRST + USER_MESSAGE_TYPE_NUM) {
        UserMessageType<?> umt = userMessageTypes[type-USER_MESSAGE_TYPE_FIRST];
        if (umt == null)
            throw new IllegalStateException("Invalid type " + type);
        return umt.createMessage(buff);
    } 
    switch (type) {
      case TYPE_PING:
        return new PingMessage(buff);
      case TYPE_BUFFER:
        return new BufferMessage(buff);
      case TYPE_BOOLEAN:
        return new BooleanMessage(buff);
      case TYPE_BYTEARRAY:
        return new ByteArrayMessage(buff);
      case TYPE_BYTE:
        return new ByteMessage(buff);
      case TYPE_CHARACTER:
        return new CharacterMessage(buff);
      case TYPE_DOUBLE:
        return new DoubleMessage(buff);
      case TYPE_FLOAT:
        return new FloatMessage(buff);
      case TYPE_INT:
        return new IntMessage(buff);
      case TYPE_LONG:
        return new LongMessage(buff);
      case TYPE_SHORT:
        return new ShortMessage(buff);
      case TYPE_STRING:
        return new StringMessage(buff);
      case TYPE_JSON_OBJECT:
        return new JsonObjectMessage(buff);
      case TYPE_BONAPORTABLE_ASCII:
          return new BonaPortableMessage(buff);
      case TYPE_JSON_ARRAY:
        return new JsonArrayMessage(buff);
      case TYPE_REPLY_FAILURE:
        return new ReplyFailureMessage(buff);
      default:
        throw new IllegalStateException("Invalid type " + type);
    }
  }
}
