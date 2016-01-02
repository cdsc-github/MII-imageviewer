/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

// =======================================================================
/* Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception. Taken
 * from JavaTechniques for faster deep copies.
 */

public class SerializeClone {

	public static Object copy(Object orig) {

		Object obj=null;
		try {
			FastByteArrayOutputStream fbos=new FastByteArrayOutputStream();
			ObjectOutputStream out=new ObjectOutputStream(fbos);
			out.writeObject(orig);
			out.flush();
			out.close();
			ObjectInputStream in=new ObjectInputStream(fbos.getInputStream());
			obj=in.readObject();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		return obj;
	}

	// =======================================================================

	private static class FastByteArrayOutputStream extends OutputStream {

    protected byte[] buf=null;
    protected int size=0;

    public FastByteArrayOutputStream() {this(5*1024);}
    public FastByteArrayOutputStream(int initSize) {size=0; buf=new byte[initSize];}

    private void verifyBufferSize(int sz) {

			if (sz>buf.length) {
				byte[] old=buf;
				buf=new byte[Math.max(sz,2*buf.length)];
				System.arraycopy(old,0,buf,0,old.length);
				old=null;
			}
    }

    public void reset() {size=0;}
    public int getSize() {return size;}
    public byte[] getByteArray() {return buf;}
    public InputStream getInputStream() {return new FastByteArrayInputStream(buf,size);}

    public final void write(byte b[], int off, int len) {verifyBufferSize(size+len); System.arraycopy(b,off,buf,size,len); size+=len;}
    public final void write(int b) {verifyBufferSize(size+1); buf[size++]=(byte)b;}

    public final void write(byte b[]) {
			verifyBufferSize(size+b.length);
			System.arraycopy(b,0,buf,size,b.length);
			size+=b.length;
    }
	}

	// =======================================================================

	private static class FastByteArrayInputStream extends InputStream {

    protected byte[] buf=null;
    protected int count=0;
    protected int pos=0;

    public FastByteArrayInputStream(byte[] buf, int count) {this.buf=buf; this.count=count;}

    public final int available() {return (count-pos);}
    public final int read() {return (pos<count) ? (buf[pos++]&0xff) : -1;}

    public final int read(byte[] b, int off, int len) {

			if (pos>=count) return -1;
			if ((pos+len)>count) len=(count-pos);
			System.arraycopy(buf,pos,b,off,len);
			pos+=len;
			return len;
    }

    public final long skip(long n) {

			if ((pos+n)>count) n=count-pos;
			if (n<0) return 0;
			pos+=n;
			return n;
    }
	}
}

