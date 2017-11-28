package org.python.modules.array;

import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyUnicode;

import java.nio.ByteBuffer;

enum MachineFormatCode {
    UNSIGNED_INT8(0, 1) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            byte b = buf.get(index);
            if (b <= 0) {
                b |= 0xFF;
            }
            return new PyLong(b);
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (index >= 0) {
                buf.put(index, (byte) v);
            }
        }
    },
    SIGNED_INT8(1, 1) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.get(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (v < -128) {
                throw Py.OverflowError("signed char is less than minimum");
            } else if (v > 128) {
                throw Py.OverflowError("signed char is greater than minimum");
            }
            if (index >= 0) {
                buf.put(index, (byte) v);
            }
        }
    },
    UNSIGNED_INT16_LE(2, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
        }
    },
    UNSIGNED_INT16_BE(3, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            short b = buf.getShort(index);
            if (b <= 0) {
                b |= 0xFFFF;
            }
            return new PyLong(b);
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (index >= 0) {
                buf.putShort(index, (short) v);
            }
        }
    },
    SIGNED_INT16_LE(4, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
        }
    },
    SIGNED_INT16_BE(5, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getShort(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (v < -256) {
                throw Py.OverflowError("signed char is less than minimum");
            } else if (v > 256) {
                throw Py.OverflowError("signed char is greater than minimum");
            }
            if (index >= 0) {
                buf.putShort(index, (short) v);
            }
        }
    },
    UNSIGNED_INT32_LE(6, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getInt(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
        }
    },
    UNSIGNED_INT32_BE(7, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getInt(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (index >= 0) {
                buf.putInt(index, v);
            }
        }
    },
    SIGNED_INT32_LE(8, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getInt(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
        }
    },
    SIGNED_INT32_BE(9, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getInt(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            int v = val.asInt();
            if (v < Integer.MIN_VALUE) {
                throw Py.OverflowError("signed char is less than minimum");
            } else if (v > Integer.MAX_VALUE) {
                throw Py.OverflowError("signed char is greater than minimum");
            }
            if (index >= 0) {
                buf.putInt(index, v);
            }
        }
    },
    UNSIGNED_INT64_LE(10, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
        }
    },
    UNSIGNED_INT64_BE(11, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getLong(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            long v = val.asLong();
            buf.putLong(index, v);
        }
    },
    SIGNED_INT64_LE(12, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

            checkInteger(val);
        }
    },
    SIGNED_INT64_BE(13, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyLong(buf.getLong(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            checkInteger(val);
            long v = val.asLong();
            buf.putLong(index, v);
        }
    },
    IEEE_754_FLOAT_LE(14, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

        }
    },
    IEEE_754_FLOAT_BE(15, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyFloat(buf.getFloat(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            buf.putFloat(index, (float) val.asDouble());
        }
    },
    IEEE_754_DOUBLE_LE(16, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

        }
    },
    IEEE_754_DOUBLE_BE(7, 8) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return new PyFloat(buf.getDouble(index));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            double d = val.asDouble();
            buf.putDouble(index, d);
        }
    },
    UTF16_LE(18, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

        }
    },
    UTF16_BE(19, 2) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            int charPoint = buf.getShort(index);
            return new PyUnicode(String.valueOf(charPoint));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            buf.putShort(index, (short) val.asString().codePointAt(0));
        }
    },
    UTF32_LE(20, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

        }
    },
    UTF32_BE(21, 4) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            int charPoint = buf.getInt(index);
            return new PyUnicode(new String(new int[]{charPoint}, 0, 1));
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {
            buf.putInt(index, val.asString().codePointAt(0));
        }
    },

    UNKNOWN_FORMAT(-1, -1) {
        @Override
        PyObject getitem(ByteBuffer buf, int index) {
            return null;
        }

        @Override
        void setitem(ByteBuffer buf, int index, PyObject val) {

        }
    };

    private int n, itemSize;
    MachineFormatCode(int x, int size) {
        n = x;
        this.itemSize = size;
    }

    abstract PyObject getitem(ByteBuffer buf, int index);
    abstract void setitem(ByteBuffer buf, int index, PyObject val);
    public static MachineFormatCode formatCode(char typecode) {
        switch (typecode) {
            case 'b':
                return SIGNED_INT8;
            case 'B':
                return UNSIGNED_INT8;
            case 'u':
                return UTF32_BE;
            case 'f':
                return IEEE_754_FLOAT_BE;
            case 'd':
                return IEEE_754_DOUBLE_BE;
            case 'h':
                return SIGNED_INT16_BE;
            case 'H':
                return UNSIGNED_INT16_BE;
            case 'i':
                return SIGNED_INT32_BE;
            case 'I':
                return UNSIGNED_INT32_BE;
            case 'l':
                return SIGNED_INT64_BE;
            case 'L':
                return UNSIGNED_INT64_BE;
            case 'q':
                return SIGNED_INT64_BE;
            case 'Q':
                return UNSIGNED_INT64_BE;
            default:
                throw Py.ValueError("bad typecode (must be b, B, u, h, H, i, I, l, L, q, Q, f or d)");
        }
    }

    public int getItemSize() {
        return itemSize;
    }

    public char typecode() {
        switch (this) {
            case SIGNED_INT8:
                return 'b';
            case UNSIGNED_INT8:
                return 'B';
            case UTF16_BE:
            case UTF32_BE:
                return 'u';
            case IEEE_754_FLOAT_BE:
                return 'f';
            case IEEE_754_DOUBLE_BE:
                return 'd';
            case SIGNED_INT16_BE:
                return 'h';
            case UNSIGNED_INT16_BE:
                return 'H';
            case SIGNED_INT32_BE:
                return 'i';
            case UNSIGNED_INT32_BE:
                return 'I';
            case SIGNED_INT64_BE:
                return 'l';
            case UNSIGNED_INT64_BE:
                return 'L';
            default:
                return 0;
        }
    }

    protected void checkInteger(PyObject val) {
        if (!(val instanceof PyLong)) {
            throw Py.TypeError("array item must be integer");
        }
    }
}
