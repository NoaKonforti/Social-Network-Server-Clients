package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.OP.*;
import bgu.spl.net.srv.ConnectionsImpl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class EncoderDecoderBGS implements MessageEncoderDecoder<Message> {

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private byte[] objectBytes = null;
    private int objectBytesIndex = 0;

    public EncoderDecoderBGS() {}



    @Override
    public Message decodeNextByte(byte nextByte) {
        if (objectBytes == null) { //indicates that we are still reading the length
            lengthBuffer.put(nextByte);
            if (!lengthBuffer.hasRemaining()) { //we read 4 bytes and therefore can take the length
                lengthBuffer.flip();
                objectBytes = new byte[lengthBuffer.getInt()];
                objectBytesIndex = 0;
                lengthBuffer.clear();
            }
        } else {
            objectBytes[objectBytesIndex] = nextByte;
            if (++objectBytesIndex == objectBytes.length) {
                Message result = decode();
                objectBytes = null;
                return result;
            }
        }
        return null;
    }

    public Message decode() {
        byte[] opcodeB = {objectBytes[0],objectBytes[1]};
        int currByte = 2;
        short opcode = bytesToShort(opcodeB);
        if (opcode == 1) { //REGISTER
            int start = 2;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String username = deserializeString(start,currByte);
            currByte++;
            start = currByte;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String password = deserializeString(start,currByte);
            currByte++;
            start = currByte;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String dateString = deserializeString(start,currByte);
            Date date = new Date();
            try {
                date = new SimpleDateFormat("dd-MM-yyyy").parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new REGISTER(username, password, date);
        }
        else if (opcode == 2) { //LOGIN
            int start = 2;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String userName = deserializeString(start, currByte);
            currByte ++;
            start = currByte;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String password = deserializeString(start, currByte);
            boolean captcha = true;
            if (objectBytes[currByte+1] != (byte)'1')
                captcha = false;
            return (new LOGIN(userName, password, captcha));
        }
        else if (opcode ==  3) { //LOGOUT
            return (new LOGOUT());
        }
        else if (opcode == 4) { //FOLLOW/UNFOLLOW
            boolean shouldFollow = true;
            char follow = (char)objectBytes[2];
            if (follow == '1')
                shouldFollow = false;
            int start = 4;
            currByte = 4;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String userName = deserializeString(start, currByte);
            return (new FOLLOW(shouldFollow, userName));
        }
        else if (opcode == 5) { //POST
            int start = 2;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String content = deserializeString(start, currByte);
            return (new POST(content));
        }
        else if (opcode == 6) { //PM
            int start = 2;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String target = deserializeString(start,currByte);
            currByte++;
            start = currByte;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String content = deserializeString(start,currByte);
            currByte++;
            start = currByte;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String date = deserializeString(start,currByte);
            return new PM(target, content, date);
        }
        else if (opcode == 7) { //LOGSTAT
            return  (new LOGSTAT());
        }
        else if (opcode == 8) { //STAT
            int start = 2;
            ArrayList<String> usersList = new ArrayList<>();
            while (objectBytes[currByte]!='\0') {
                if (objectBytes[currByte]=='|'){
                    String userName = deserializeString(start, currByte);
                    usersList.add(userName);
                    currByte++;
                    start = currByte;
                }
                else currByte++;
            }
            String userName = deserializeString(start, currByte);
            usersList.add(userName);
            return (new STAT(usersList));
        }
        else if (opcode == 12) { //BLOCK
            int start = 2;
            while (objectBytes[currByte]!='\0') {
                currByte++;
            }
            String target = deserializeString(start,currByte);
            return new BLOCK(target);
        }
        return null; //Else - Illegal Input
    }

    private String deserializeString(int start, int end) {
        byte[] stringBytes = Arrays.copyOfRange(objectBytes, start,(end + 1));
        String ret = "";
        while (start < end) {
            ret = ret + (char)objectBytes[start];
            start++;
        }
        //String ret = new String(stringBytes, StandardCharsets.US_ASCII);
        return ret;
//        try {
//            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(stringBytes));
//            return (String) in.readObject();
//        } catch (Exception ex) {
//            throw new IllegalArgumentException("cannot deserialize String", ex);
//        }
    }

    @Override
    public byte[] encode(Message message) {
        byte[] op = shortToBytes(message.getOpCode());
        //byte[] separator = stringEncode("\0");
        byte[] separator = {(byte)'\0'};
        //byte[] delimiter = stringEncode(";");
        byte[] delimiter = {(byte)';'};
        if (message.getOpCode() == 9) { //NOTIFICATION
            byte[] type = {(byte)(((NOTIFICATION)message).getType())};
            byte[] postingUser = stringEncode(((NOTIFICATION)message).getPostingUser());
            byte[] content = stringEncode(((NOTIFICATION)message).getContent());
            byte[] ret = new byte[op.length + type.length + postingUser.length + content.length + delimiter.length];
            int index = 0;
            for (int i = 0; i < op.length; i++) {
                ret[index] = op[i];
                index++;
            }
            for (int i = 0; i < type.length; i++) {
                ret[index] = type[i];
                index++;
            }
            for (int i = 0; i < postingUser.length; i++) {
                ret[index] = postingUser[i];
                index++;
            }
            for (int i = 0; i < content.length; i++) {
                ret[index] = content[i];
                index++;
            }
            for (int i = 0; i < delimiter.length; i++) {
                ret[index] = delimiter[i];
                index++;
            }
            return ret;
        }
        else if (message.getOpCode() == 10) { //ACK
            byte[] messageOp = shortToBytes(((ACK)message).getSuccessOp());
            String optional = ((ACK)message).getOptional();
            byte[] ret;
            byte[] optionalBytes;
            if (optional != null) {
                optionalBytes = stringEncode(optional);
                ret = new byte[op.length + messageOp.length + optionalBytes.length + 1];
            }
            else {
                ret = new byte[op.length + messageOp.length + 1];
                optionalBytes = null;
            }
            int index = 0;
            for (int i = 0; i < op.length; i++) {
                ret[index] = op[i];
                index++;
            }
            for (int i = 0; i < messageOp.length; i++) {
                ret[index] = messageOp[i];
                index++;
            }
            if (optional != null) {
                for (int i = 0; i < optionalBytes.length; i++) {
                    ret[index] = optionalBytes[i];
                    index++;
                }
            }
            ret[ret.length-1] = delimiter[0];
            return ret;
        }
        else if (message.getOpCode() == 11) { //ERROR
            byte[] messageOp = shortToBytes(((ERROR)message).getFailedOp());
            byte[] ret = new byte[op.length + messageOp.length + delimiter.length];
            int index = 0;
            for (int i = 0; i < op.length; i++) {
                ret[index] = op[i];
                index++;
            }
            for (int i = 0; i < messageOp.length; i++) {
                ret[index] = messageOp[i];
                index++;
            }
            for (int i = 0; i < delimiter.length; i++) {
                ret[index] = delimiter[i];
                index++;
            }
            return ret;
        }
        return null;
    }

    private byte[] stringEncode(String message) {
//        try {
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

//            //placeholder for the object size
//            for (int i = 0; i < 4; i++) {
//                bytes.write(0);
//            }
//
//            ObjectOutput out = new ObjectOutputStream(bytes);
//            out.writeObject(message);
//            out.flush();

//            byte[] result = bytes.toByteArray();

            //now write the object size
//            ByteBuffer.wrap(result).putInt(result.length);
            byte[] result = new byte[message.length() + 1];
            for (int i = 0; i < message.length(); i++)
                result[i] = (byte)(message.charAt(i));
            result[result.length-1] = '\0';
            return result;
//        } catch (Exception ex) {
//            throw new IllegalArgumentException("cannot serialize message", ex);
//        }
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
