package connecter.buffer;

public interface IBuffer {

	int get();

	int get(byte[] dest);

	int get(byte[] dest, int offset, int length);

	void put(int c);

	void put(byte[] src);

	void put(byte[] src, int offset, int length);

	int remaining();
	
	int size();

	boolean readFromChannel();

	void ready4WriteToChannel();

	boolean writeToChannel();

	void prepare(int size);
	
	IBuffer getAdhering(int contentLength);
}