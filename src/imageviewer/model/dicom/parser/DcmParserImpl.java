/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package imageviewer.model.dicom.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.InflaterInputStream;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import org.xml.sax.ContentHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmParserImpl implements DcmParser {
    private static final Logger log = Logger.getLogger(DcmParserImpl.class);
    
    private static final int TS_ID_TAG = 0x00020010;
    private static final int ITEM_TAG = 0xFFFEE000;
    private static final int ITEM_DELIMITATION_ITEM_TAG = 0xFFFEE00D;
    private static final int SEQ_DELIMITATION_ITEM_TAG = 0xFFFEE0DD;
    
    private final byte[] b0 = new byte[0];
    private final byte[] b12 = new byte[12];
    private final ByteBuffer bb12 = 
            ByteBuffer.wrap(b12).order(ByteOrder.LITTLE_ENDIAN);
//    private boolean explicitVR = false;
    private DcmDecodeParam decodeParam = DcmDecodeParam.IVR_LE;
    private int maxAlloc = 0x4000000; // 64MB
        
    private DataInput in = null;
    private DcmHandler handler = null;
    private VRMap vrMap = VRMap.DEFAULT;
   
    private int rTag = -1;
    private int rVR = -1;
    private int rLen = -1;
    private long rPos = 0L;
    private boolean eof = false;
    private String tsUID = null;
    
    private ByteArrayOutputStream unBuf = null;
    
    public DcmParserImpl(InputStream in) {
        this.in = in instanceof DataInput ? (DataInput)in
                                          : new DataInputStream(in);
    }

    public DcmParserImpl(ImageInputStream in) {        
        if (in == null) {
            throw new NullPointerException();
        }
        this.in = in;
    }

    public InputStream getInputStream() {
        return (InputStream)in;
    }

    public ImageInputStream getImageInputStream() {
        return (ImageInputStream)in;
    }

    public final int getReadTag() {
        return rTag;
    }
        
    public final int getReadVR() {
        return rVR;
    }
        
    public final int getReadLength() {
        return rLen;
    }
        
    public final long getStreamPosition() {
        return rPos;
    }

    public final void setStreamPosition(long rPos) {
        this.rPos = rPos;
    }
    public final void seek(long pos) throws IOException {
        if (!(in instanceof ImageInputStream)) {
            throw new UnsupportedOperationException();
        }
        ((ImageInputStream)in).seek(pos);
        this.rPos = pos;
    }
    
    public final boolean hasSeenEOF() {
        return eof;
    }
        
    public final void setDcmHandler(DcmHandler handler) {
        this.handler = handler;
    }
    
    /*
    public final void setSAXHandler(ContentHandler hc, TagDictionary dict) {
        this.handler = new DcmHandlerAdapter(hc, dict);
    }

    public final void setSAXHandler2(ContentHandler ch, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir) {
        this.handler = new DcmHandlerAdapter2(ch, dict, excludeTags,
                excludeValueLengthLimit, basedir);
    }
    */
    /*    
    public final void setInput(InputStream in) {
        if (in != null) {
            this.in = in instanceof DataInput ? (DataInput)in
                                              : new DataInputStream(in);
        } else {
            this.in = null;
        }
    }

    public final void setInput(ImageInputStream in) {        
        this.in = in;
    }
*/    
    public final void setVRMap(VRMap vrMap) {
        if (vrMap == null)
            throw new NullPointerException();
        this.vrMap = vrMap;
    }    

    public final void setDcmDecodeParam(DcmDecodeParam param) {
        if (log.isDebugEnabled())
            log.debug(param.toString());
        if (param.deflated != decodeParam.deflated) {
            if (!param.deflated)
                throw new UnsupportedOperationException(
                        "Cannot remove Inflater");
            else
                in = new DataInputStream(new InflaterInputStream(
                        in instanceof InputStream ? (InputStream)in
                            : new InputStreamAdapter((ImageInputStream)in)));
        }
        bb12.order(param.byteOrder);
        decodeParam = param;
    }
    
    public final DcmDecodeParam getDcmDecodeParam() {
        return decodeParam;
    }

    private String logMsg() {
        return "rPos:" + rPos + " " + Tags.toString(rTag) 
                + " " + VRs.toString(rVR)
                + " #" + rLen;
    }
    
    public FileFormat detectFileFormat() throws IOException { 
        FileFormat retval = null;
        if (in instanceof InputStream)
            retval = detectFileFormat((InputStream)in);        
        else if (in instanceof ImageInputStream)
            retval = detectFileFormat((ImageInputStream)in);
        else
            throw new UnsupportedOperationException("" + in);
        if (log.isDebugEnabled())
            log.debug("detect " + retval);
        return retval;
    }
    
    private int testEVRFormat(DcmDecodeParam decodeParam) throws IOException {
        setDcmDecodeParam(decodeParam);
        parseHeader();
        if (rVR == vrMap.lookup(rTag)) {
            if ((rTag >>> 16) == 2)
                return 0;
            if (rTag >= 0x00080000)
                return 1;
        }
        return -1;
    }
    
    private int testIVRFormat(DcmDecodeParam decodeParam) throws IOException {
        setDcmDecodeParam(decodeParam);
        parseHeader();
        if (rVR != VRs.UN && rLen <= 64) {
            if ((rTag >>> 16) == 2)
                return 0;
            if (rTag >= 0x00080000)
                return 1;
        }
        return -1;
    }
    
    private FileFormat detectFileFormat(InputStream in) throws IOException {
        in.mark(144);
        try {
            switch (testIVRFormat(DcmDecodeParam.IVR_LE)) {
                case 0: return FileFormat.IVR_LE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.ACRNEMA_STREAM;
            }
            in.reset();
            switch (testEVRFormat(DcmDecodeParam.EVR_LE)) {
                case 0: return FileFormat.DICOM_FILE_WO_PREAMBLE;
                case 1: return FileFormat.EVR_LE_STREAM;
            }
            in.reset();
            switch (testEVRFormat(DcmDecodeParam.EVR_BE)) {
                case 0: return FileFormat.EVR_BE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.EVR_BE_STREAM;
            }
            in.reset();
            switch (testIVRFormat(DcmDecodeParam.IVR_BE)) {
                case 0: return FileFormat.IVR_BE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.IVR_BE_STREAM;
            }
            in.reset();
            if (in.skip(128) != 128 || in.read() != 'D' || in.read() != 'I'
                                    || in.read() != 'C' || in.read() != 'M')
                return null;
            if (testEVRFormat(DcmDecodeParam.EVR_LE) == 0)
                return FileFormat.DICOM_FILE;
            in.reset();
            if (in.skip(132) != 132)
                return null;
            if (testIVRFormat(DcmDecodeParam.IVR_LE) == 0)
                return FileFormat.IVR_LE_FILE;
            in.reset();
            if (in.skip(132) != 132)
                return null;
            if (testEVRFormat(DcmDecodeParam.EVR_BE) == 0)
                return FileFormat.EVR_BE_FILE;
            in.reset();
            if (in.skip(132) != 132)
                return null;
            if (testEVRFormat(DcmDecodeParam.IVR_BE) == 0)
                return FileFormat.IVR_BE_FILE;
        } finally {
            in.reset();
        }
        return null;
    }

    private FileFormat detectFileFormat(ImageInputStream in)
            throws IOException {
        in.mark();
        try {
            switch (testIVRFormat(DcmDecodeParam.IVR_LE)) {
                case 0: return FileFormat.IVR_LE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.ACRNEMA_STREAM;
            }
            in.reset();in.mark();
            switch (testEVRFormat(DcmDecodeParam.EVR_LE)) {
                case 0: return FileFormat.DICOM_FILE_WO_PREAMBLE;
                case 1: return FileFormat.EVR_LE_STREAM;
            }
            in.reset();in.mark();
            switch (testEVRFormat(DcmDecodeParam.EVR_BE)) {
                case 0: return FileFormat.EVR_BE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.EVR_BE_STREAM;
            }
            in.reset();in.mark();
            switch (testIVRFormat(DcmDecodeParam.IVR_BE)) {
                case 0: return FileFormat.IVR_BE_FILE_WO_PREAMBLE;
                case 1: return FileFormat.IVR_BE_STREAM;
            }
            in.reset();in.mark();
            if (in.skipBytes(128) != 128 || in.read() != 'D' || in.read() != 'I'
                                      || in.read() != 'C' || in.read() != 'M') 
                return null;
            in.mark();
            try {
                if (testEVRFormat(DcmDecodeParam.EVR_LE) == 0)
                    return FileFormat.DICOM_FILE;
                in.reset();in.mark();
                if (testIVRFormat(DcmDecodeParam.IVR_LE) == 0)
                    return FileFormat.IVR_LE_FILE;
                in.reset();in.mark();
                if (testEVRFormat(DcmDecodeParam.EVR_BE) == 0)
                    return FileFormat.EVR_BE_FILE;
                in.reset();in.mark();
                if (testEVRFormat(DcmDecodeParam.IVR_BE) == 0)
                    return FileFormat.IVR_BE_FILE;
            } finally {
                in.reset();
            }
        } finally {
            in.reset();
        }
       throw new DcmParseException("Unknown Format");
    }

    public int parseHeader() throws IOException {
        eof = false;
        try {
            b12[0] = in.readByte();
        } catch (EOFException ex) {
            eof = true;
            log.debug("Detect EOF");
            return -1;
        }
        in.readFully(b12, 1, 7);
        rPos += 8;
        rTag = (bb12.getShort(0) << 16) | (bb12.getShort(2) & 0xffff);
        int retval = 8;
        switch (rTag) {
            case ITEM_TAG:
            case ITEM_DELIMITATION_ITEM_TAG:
            case SEQ_DELIMITATION_ITEM_TAG:
                rVR = VRs.NONE;
                rLen = bb12.getInt(4);
                break;
            default:
                if (!decodeParam.explicitVR) {
                    rVR = vrMap.lookup(rTag);
                    rLen = bb12.getInt(4);
                } else {
                    rVR = (bb12.get(4) << 8) | (bb12.get(5) & 0xff);
                    if (VRs.isLengthField16Bit(rVR)) {
                        rLen = bb12.getShort(6) & 0xffff;
                    } else {
                        in.readFully(b12, 8, 4);
                        rPos += 4;
                        rLen = bb12.getInt(8);
                        retval = 12;
                    }
                }
        }
        if (unBuf != null)
            unBuf.write(b12, 0, retval);
        if (log.isDebugEnabled())
            log.debug(logMsg());
        return retval;
    }

    private byte[] parsePreamble() throws IOException {
        log.debug("rPos:" + rPos);

        byte[] b128 = new byte[128];        
        in.readFully(b128,0,128);
        rPos += 128;
        in.readFully(b12, 0, 4);
        rPos += 4;
        if (b12[0] != (byte)'D' || b12[1] != (byte)'I'
                || b12[2] != (byte)'C' || b12[3] != (byte)'M')
            throw new DcmParseException("Missing DICM Prefix");

        return b128;
    }
    
    public long parseFileMetaInfo(boolean preamble, DcmDecodeParam param)
            throws IOException {
        rPos = 0L;
        byte[] data = preamble ? parsePreamble() : null;
        if (handler != null)
            handler.startFileMetaInfo(data);
        
        setDcmDecodeParam(param);
        parseGroup(2);
        if (handler != null)
            handler.endFileMetaInfo();
        return rPos;
    }

    public long parseFileMetaInfo() throws IOException {
        return parseFileMetaInfo(true, DcmDecodeParam.EVR_LE);
    }
    
    public long parseCommand() throws IOException {
        if (handler != null)
            handler.startCommand();
                
        setDcmDecodeParam(DcmDecodeParam.IVR_LE);
        long read = parseGroup(0);
        if (handler != null)
            handler.endCommand();
        return read;
    }

    private long parseGroup(int groupTag) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("parse group " + groupTag);
        }
        if (handler != null)
            handler.setDcmDecodeParam(decodeParam);

        final long rPos0 = rPos;
        final int hlen = parseHeader();
        if (hlen != 8 || (rTag >>> 16) != groupTag || rVR != VRs.UL
                || rLen != 4)
            throw new DcmParseException("hlen=" + hlen + ", " + logMsg());

        in.readFully(b12, 0, 4);
        rPos += 4;
        if (handler != null) {
            handler.startElement(rTag, rVR, rPos0);
            byte[] b4 = new byte[4];
            System.arraycopy(b12, 0, b4, 0, 4);
            handler.value(b4, 0, 4);
            handler.endElement();
        }
        return doParse(-1, bb12.getInt(0)) + 12;
    }
/*    
    public long parse(int stopTag, int length) throws IOException {
        if (handler != null)
            handler.setDcmDecodeParam(decodeParam);
        return doParse(stopTag, length);
    }
*/
    public long parseDataset(String tuid, int stopTag)
      throws IOException
    {
        return parseDataset(DcmDecodeParam.valueOf(tuid), stopTag);
    }
               
    public long parseDataset(DcmDecodeParam param, int stopTag)
            throws IOException {
        if (param != null)
            setDcmDecodeParam(param);
        if (handler != null) {
            handler.startDataset();
            handler.setDcmDecodeParam(decodeParam);
        }
        long read = doParse(stopTag, -1);
        if (handler != null)
            handler.endDataset();
        return read;
    }

    public long parseDcmFile(FileFormat format, int stopTag)
            throws IOException {
       if (format == null) {
           format = detectFileFormat();
           if (format == null) {
               log.info("Failed to detect format - try decode as ACR/NEMA Stream");
               format = FileFormat.ACRNEMA_STREAM;
           }
       }
       if (handler != null)
           handler.startDcmFile();
       DcmDecodeParam param = format.decodeParam;
       rPos = 0L;
       if (format.hasFileMetaInfo) {
           tsUID = null;
           parseFileMetaInfo(format.hasPreamble, format.decodeParam);
           if (tsUID == null)
               log.warn("Missing Transfer Syntax UID in FMI");
           else
               param = DcmDecodeParam.valueOf(tsUID);
       }
       parseDataset(param, stopTag);
       if (handler != null)
           handler.endDcmFile();
       return rPos;
    }
    
    public long parseItemDataset() throws IOException {
        in.readFully(b12, 0, 8);
        rPos += 8;
        int itemtag = (bb12.getShort(0) << 16)
                    | (bb12.getShort(2) & 0xffff);
        int itemlen = bb12.getInt(4);
        if (itemtag == SEQ_DELIMITATION_ITEM_TAG) {
            if (itemlen != 0) {
                throw new DcmParseException(
                                    "(fffe,e0dd), Length:" + itemlen);
            }
            return -1L;
        }
        if (itemtag != ITEM_TAG) {
            throw new DcmParseException(Tags.toString(itemtag));
        }
        if (log.isDebugEnabled()) {
            log.debug("rpos:" + (rPos-8) + ",(fffe,e0dd)");
        }
        if (handler != null) {
            handler.startDataset();
        }
        long lread;
        if (itemlen == -1) {
            lread = doParse(ITEM_DELIMITATION_ITEM_TAG, itemlen);
            if (rTag != ITEM_DELIMITATION_ITEM_TAG || rLen != 0)
                throw new DcmParseException(logMsg());
        } else {
            lread = doParse(-1, itemlen);
        }
        if (handler != null)
            handler.endDataset();
        return 8 + lread;
    }
    
    private long doParse(int stopTag, int length) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rpos:" + rPos
                    + ",stopTag:" + Tags.toString(stopTag)
                    + ",length:" + length);
        }
        long lread = 0;        
        if (length != 0) {
            long llen = length & 0xffffffffL;
            loop: do {
                final long rPos0 = rPos;
                final int hlen = parseHeader();
                if (hlen == -1) {
                    if (length != -1)
                        throw new EOFException();
                    break loop;
                }
                lread += hlen;
                if (stopTag == rTag)
                    break loop;
                if (handler != null && unBuf == null
                        && rTag != ITEM_DELIMITATION_ITEM_TAG)
                    handler.startElement(rTag, rVR, rPos0);

                if (rLen == -1 || rVR == VRs.SQ) {
                    switch (rVR) {
                        case VRs.SQ:
                        case VRs.OB: case VRs.OF: case VRs.OW:
                        case VRs.UN:
                            break;
                        default:
                            throw new DcmParseException(logMsg());
                    }                    
                    lread += parseSequence(rVR, rLen);
                } else {
                    readValue();
                    lread += rLen;
                }
                if (handler != null && unBuf == null)
                    handler.endElement();
            } while (length == -1 || lread < llen);
            if (length != -1 && lread > llen)
                log.info(logMsg()
                        + ", value length extend specified read length " + llen
                        + " for " + (lread-llen) + " Bytes!");
        }
        return lread;
    }

    private long parseSequence(int vr, int sqLen) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rPos:" + rPos + "," + VRs.toString(vr)
                    + " #" + sqLen);
        }
        if (handler != null && unBuf == null)
            handler.startSequence(sqLen);
        long lread = 0;        
        if (sqLen != 0) {
            long llen = sqLen & 0xffffffffL;
            int id = 0;
            loop: do {
                in.readFully(b12, 0, 8);
                rPos += 8;
                if (unBuf != null)
                    unBuf.write(b12, 0, 8);
                lread += 8;
                int itemtag = (bb12.getShort(0) << 16)
                            | (bb12.getShort(2) & 0xffff);
                int itemlen = bb12.getInt(4);
                switch (itemtag) {
                    case SEQ_DELIMITATION_ITEM_TAG:
                        if (sqLen != -1) {
                            log.warn("Unexpected Sequence Delimination Item"
                                + " (fffe,e0dd) for Sequence with explicit length: "
                                + sqLen);
                        }
                        if (itemlen != 0)
                            throw new DcmParseException(
                                    "(fffe,e0dd), Length:" + itemlen);
                        break loop;
                    case ITEM_TAG:
                        lread += parseItem(++id, vr, itemlen);
                        break;
                    default:
                        throw new DcmParseException(
                                Tags.toString(itemtag));
                }
            } while (sqLen == -1 || lread < llen);
            if (sqLen != -1 && lread > llen)
                throw new DcmParseException(logMsg() + ", Read: " + lread
                        + ", Length: " + llen);
        }
//        rLen = sqLen; // restore rLen value
        if (handler != null && unBuf == null)
            handler.endSequence(sqLen);
        return lread;
    }
            
    private long parseItem(int id, int vr, int itemlen) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("rPos:" + rPos + "," + VRs.toString(vr)
                    + " #" + itemlen);
        }
        switch (vr) {
            case VRs.SQ:
                return parseSQItem(id, itemlen);
            case VRs.UN:
                if (itemlen == -1)
                    return parseUNItem(id, itemlen);
                // fall through
            case VRs.OB: case VRs.OF: case VRs.OW:
                return readFragment(id, itemlen);
            default:
                throw new RuntimeException(logMsg());
        }
   }

    private long parseUNItem(int id, int itemlen) throws IOException {
        long retval;
        if (unBuf != null)
            retval = parseSQItem(id, itemlen);
        else {
            long rPos0 = rPos;
            unBuf = new ByteArrayOutputStream();
            final DcmDecodeParam tmpDecodeParam = decodeParam;
            try {
                setDcmDecodeParam(DcmDecodeParam.IVR_LE);
                bb12.order(ByteOrder.LITTLE_ENDIAN);
                retval = parseSQItem(id, itemlen);
                if (handler != null) {
                    handler.fragment(id, rPos0-8, unBuf.toByteArray(), 0,
                            unBuf.size()-8);
                }
            } finally {
                setDcmDecodeParam(tmpDecodeParam);
                unBuf = null;
            }
        }
        return retval;
    }

    private long parseSQItem(int id, int itemlen) throws IOException {
        if (handler != null && unBuf == null)
            handler.startItem(id, rPos-8, itemlen);

        long lread;
        if (itemlen == -1) {
            lread = doParse(ITEM_DELIMITATION_ITEM_TAG, itemlen);
            if (rTag != ITEM_DELIMITATION_ITEM_TAG || rLen != 0)
                throw new DcmParseException(logMsg());
        } else
            lread = doParse(-1, itemlen);

        if (handler != null && unBuf == null)
            handler.endItem(itemlen);

        return lread;
    }
    
    private int readValue() throws IOException {
        byte[] data = readBytes(rLen);
        if (handler != null && unBuf == null)
            handler.value(data, 0, rLen);
        if (rTag == TS_ID_TAG)
            tsUID = decodeUID(data, rLen-1);
        return rLen;
    }
    
    private String decodeUID(byte[] data, int rlen1) {
        if (rlen1 < 0) {
            log.warn("Empty Transfer Syntax UID in FMI");
            return "";
        }
        try {
            return new String(data, 0, data[rlen1] == 0 ? rlen1 : rlen1+1,
                        "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            log.warn("Decoding Transfer Syntax UID in FMI failed!", ex);
            return null;
        }
    }
    
    private int readFragment(int id, int itemlen) throws IOException {
        long rPos0 = rPos;
        byte[] data = readBytes(itemlen);
        if (handler != null && unBuf == null)
            handler.fragment(id, rPos0-8, data, 0, itemlen);
        return itemlen;
    }
    
    private byte[] readBytes(int len) throws IOException {
        if (len == 0)
            return b0;
        if (len < 0 || len > maxAlloc)
            throw new DcmParseException(logMsg() + ", MaxAlloc:" + maxAlloc);
        byte[] retval = new byte[len];
        in.readFully(retval, 0, len);
        rPos += len;
        if (unBuf != null)
            unBuf.write(retval, 0, len);
        return retval;
    }    
}
