package name.kghost.oauth.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {
	private ServletOutputStream output;
	private int status;
	private List<byte[]> data;

	public ResponseWrapper(HttpServletResponse response) throws IOException {
		super(response);
		data = new LinkedList<byte[]>();
		output = new FilterServletOutputStream(response.getOutputStream());
	}

	private class FilterServletOutputStream extends ServletOutputStream {
		private OutputStream stream;

		public FilterServletOutputStream(OutputStream output) {
			stream = output;
		}

		@Override
		public void write(int b) throws IOException {
			if (isOk()) {
				byte[] e = new byte[1];
				e[0] = (byte) b;
				data.add(e);
			}
			stream.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			if (isOk()) {
				data.add(b.clone());
			}
			stream.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (isOk()) {
				byte[] e = new byte[len];
				System.arraycopy(b, off, e, 0, len);
				data.add(e);
			}
			stream.write(b, off, len);
		}
	}

	@Override
	public void setStatus(int sc) {
		status = sc;
		super.setStatus(sc);
	}
	
	public boolean isOk() {
		return status == 0 || status == 200;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return output;
	}

	@Override
	public PrintWriter getWriter() {
		return new PrintWriter(getOutputStream(), true);
	}

	public String getData() throws UnsupportedEncodingException {
		int length = 0;
		for (byte[] b : data) {
			length += b.length;
		}
		byte[] r = new byte[length];
		int p = 0;
		for (byte[] b : data) {
			System.arraycopy(b, 0, r, p, b.length);
			p += b.length;
		}
		return new String(r, "UTF-8");
	}
}
