package org.python.internal.keccak;

public class Shake256 extends KeccackSponge{
	private final static byte DOMAIN_PADDING = 0xf;
	private final static int DOMMAIN_PADDING_LENGTH = 4;

	public Shake256() {
		super(512, DOMAIN_PADDING, DOMMAIN_PADDING_LENGTH);
	}
}