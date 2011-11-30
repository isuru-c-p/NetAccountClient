package nz.ac.auckland.netlogin.util;

public class MessagePrefixUtil {

    public static byte[] addMessagePrefix(byte[] messagePrefix, byte[] message) {
        byte[] fullMessage = new byte[messagePrefix.length + message.length];
        System.arraycopy(messagePrefix, 0, fullMessage, 0, messagePrefix.length);
        System.arraycopy(message, 0, fullMessage, messagePrefix.length, message.length);
        return fullMessage;
    }

    public static byte[] removeMessagePrefix(byte[] messagePrefix, byte[] fullMessage) {
        Integer offset = findStartOfMessage(messagePrefix, fullMessage);
        if (offset == 0) throw new RuntimeException("Message is malformed");

        byte[] message = new byte[fullMessage.length - offset];
        System.arraycopy(fullMessage, offset, message, 0, message.length);
        return message;
    }

    public static boolean validateMessagePrefix(byte[] messagePrefix, byte[] fullMessage, int offset) {
        if (fullMessage.length < messagePrefix.length) return false;
        for (int i = 0; i < messagePrefix.length; i++) {
            if (fullMessage[offset + i] != messagePrefix[i]) return false;
        }
        return true;
    }

    public static Integer findStartOfMessage(byte[] messagePrefix, byte[] fullMessage) {
        Integer offset = null;
        for (int i=0; i<fullMessage.length - messagePrefix.length; i++) {
            if (MessagePrefixUtil.validateMessagePrefix(messagePrefix, fullMessage, i)) {
                offset = i + messagePrefix.length;
            }
        }
        return offset;

    }

}