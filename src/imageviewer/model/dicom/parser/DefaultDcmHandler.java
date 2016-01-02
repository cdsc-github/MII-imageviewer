/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom.parser;

import java.io.IOException;

public class DefaultDcmHandler implements DcmHandler {

	public void startCommand() throws IOException {
	}

	public void endCommand() throws IOException {
	}

	public void startDcmFile() throws IOException {
	}

	public void endDcmFile() throws IOException {
	}

	public void startFileMetaInfo(byte[] preamble) throws IOException {
	}

	public void endFileMetaInfo() throws IOException {
	}

	public void startDataset() throws IOException {
	}

	public void endDataset() throws IOException {
	}

	public void setDcmDecodeParam(DcmDecodeParam param) {
	}

	public void startElement(int tag, int vr, long pos) throws IOException {
	}

	public void endElement() throws IOException {
	}

	public void startSequence(int length) throws IOException {
	}

	public void endSequence(int length) throws IOException {
	}

	public void startItem(int id, long pos, int length) throws IOException {
		// TODO Auto-generated method stub

	}

	public void endItem(int len) throws IOException {
		// TODO Auto-generated method stub

	}

	public void value(byte[] data, int start, int length) throws IOException {
		// TODO Auto-generated method stub

	}

	public void fragment(int id, long pos, byte[] data, int start, int length)
			throws IOException {
		// TODO Auto-generated method stub

	}

}
