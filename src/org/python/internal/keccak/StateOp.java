package org.python.internal.keccak;

public enum StateOp {
	ZERO, GET, VALIDATE, XOR_IN, XOR_TRANSFORM, WRAP, UNWRAP;

	public boolean isIn() {
		return (this == XOR_IN || this == XOR_TRANSFORM || this == WRAP || this == UNWRAP || this == VALIDATE);
	}

	public boolean isOut() {
		return (this == GET || this == XOR_TRANSFORM || this == WRAP || this == UNWRAP);
	}

}
