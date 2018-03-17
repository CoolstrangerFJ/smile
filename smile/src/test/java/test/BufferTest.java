package test;

import java.nio.ByteBuffer;

public class BufferTest {

	public static void main(String[] args) {
		ByteBuffer allocate = ByteBuffer.allocate(10);
		int limit = allocate.limit();
		System.out.println("limit: "+limit);
		int capacity = allocate.capacity();
		System.out.println("capacity: "+ capacity);
		byte[] bytes = "0123456789".getBytes();
		System.out.println("length: "+ bytes.length);
		int position = 0;
		position = allocate.position();
		System.out.println("position: " + position);
		allocate.put(bytes);
		position = allocate.position();
		System.out.println("position: " + position);
		//内部为position++,指针当前位置未操作
		byte b = allocate.get(9);
		System.out.println((char)b);
	}
}
