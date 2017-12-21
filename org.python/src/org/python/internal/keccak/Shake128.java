package org.python.internal.keccak;

public final class Shake128 extends KeccackSponge {
	private final static byte DOMAIN_PADDING = 0xf;
	private final static int DOMMAIN_PADDING_LENGTH = 4;

	public Shake128() {
		super(256, DOMAIN_PADDING, DOMMAIN_PADDING_LENGTH);
	}



}