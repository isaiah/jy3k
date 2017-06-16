package org.python.modules._struct;

public class FormatCode {
    public FormatCode(FormatDef fmtdef, int repeat, char format) {
        this.fmtdef = fmtdef;
        this.repeat = repeat;
        this.format = format;
    }

    FormatDef fmtdef;
    int repeat;
    char format;
}
