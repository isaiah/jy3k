// Copyright (c) Corporation for National Research Initiatives
package org.python.compiler;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.python.bootstrap.Import;
import org.python.compiler.ProxyCodeHelpers.AnnotationDescr;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClassFile
{
    ClassWriter cw;
    int access;
    long mtime;
    public String name;
    String superclass;
    String sfilename;
    String[] interfaces;
    List<MethodVisitor> methodVisitors;
    List<FieldVisitor> fieldVisitors;
    List<AnnotationVisitor> annotationVisitors;

    public static String fixName(String n) {
        if (n.indexOf('.') == -1)
            return n;
        char[] c = n.toCharArray();
        for(int i=0; i<c.length; i++) {
            if (c[i] == '.') c[i] = '/';
        }
        return new String(c);
    }

    public static void visitAnnotations(AnnotationVisitor av, Map<String, Object> fields) {
        for (Entry<String, Object>field: fields.entrySet()) {
            visitAnnotation(av, field.getKey(), field.getValue());
        }
    }
    
    // See org.objectweb.asm.AnnotationVisitor for details
    // TODO Support annotation annotations and annotation array annotations
    public static void visitAnnotation(AnnotationVisitor av, String fieldName, Object fieldValue) {
        Class<?> fieldValueClass = fieldValue.getClass();
        
        if (fieldValue instanceof Class) {
            av.visit(fieldName, Type.getType((Class<?>)fieldValue));
        } else if (fieldValueClass.isEnum()) {
            av.visitEnum(fieldName, ProxyCodeHelpers.mapType(fieldValueClass), fieldValue.toString());
        } else if (fieldValue instanceof List) {
            AnnotationVisitor arrayVisitor = av.visitArray(fieldName);
            List<Object> fieldList = (List<Object>)fieldValue;
            for (Object arrayField: fieldList) {
                visitAnnotation(arrayVisitor, null, arrayField);
            }
            arrayVisitor.visitEnd();
        } else {
            av.visit(fieldName, fieldValue);
        }
    }

    public ClassFile(String name) {
        this(name, "java/lang/Object", Opcodes.ACC_SYNCHRONIZED | Opcodes.ACC_PUBLIC,
                -1);
    }

    public ClassFile(String name, String superclass, int access) {
        this(name, superclass, access, -1);
    }
    public ClassFile(String name, String superclass, int access, long mtime) {
        name = fixName(name);
        // cannot turn python package name to java package, because the class have to be in org.python.core to be
        // accessible by Py.java
        this.name = "org/python/core/" + name.replaceAll("/", "__");
        this.superclass = fixName(superclass);
        this.interfaces = new String[0];
        this.access = access;
        this.mtime = mtime;
        
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        methodVisitors = Collections.synchronizedList(new ArrayList<MethodVisitor>());
        fieldVisitors = Collections.synchronizedList(new ArrayList<FieldVisitor>());
        annotationVisitors = Collections.synchronizedList(new ArrayList<AnnotationVisitor>());
    }

    public void setSource(String name) {
        sfilename = name;
    }

    public void addInterface(String name) {
        String[] new_interfaces = new String[interfaces.length+1];
        System.arraycopy(interfaces, 0, new_interfaces, 0, interfaces.length);
        new_interfaces[interfaces.length] = name;
        interfaces = new_interfaces;
    }

    public Code addMethod(String name, String type, int access)
    {
        MethodVisitor mv = cw.visitMethod(access, name, type, null, null);
        Code pmv = new Code(mv, type, access);
        methodVisitors.add(pmv);
        return pmv;
    }
    public Code addMethod(String name, String type, int access, String[] exceptions)

        {
            MethodVisitor mv = cw.visitMethod(access, name, type, null, exceptions);
            Code pmv = new Code(mv, type, access);
            methodVisitors.add(pmv);
            return pmv;
        }
    
    public Code addMethod(String name, String type, int access, String[] exceptions,
            AnnotationDescr[]methodAnnotationDescrs, AnnotationDescr[][] parameterAnnotationDescrs)

    {
        MethodVisitor mv = cw.visitMethod(access, name, type, null, exceptions);

        // method annotations
        for (AnnotationDescr ad: methodAnnotationDescrs) {
            AnnotationVisitor av = mv.visitAnnotation(ad.getName(), true);
            if (ad.hasFields()) {
                visitAnnotations(av, ad.getFields());
            }
            av.visitEnd();
        }
        
        // parameter annotations
        for (int i = 0; i < parameterAnnotationDescrs.length; i++) {
            for (AnnotationDescr ad: parameterAnnotationDescrs[i]) {
                AnnotationVisitor av = mv.visitParameterAnnotation(i, ad.getName(), true);
                if (ad.hasFields()) {
                    visitAnnotations(av, ad.getFields());
                }
                av.visitEnd();
            }
        }
        
        Code pmv = new Code(mv, type, access);
        methodVisitors.add(pmv);
        return pmv;
    }
    
    public void addClassAnnotation(AnnotationDescr annotationDescr) {
        AnnotationVisitor av = cw.visitAnnotation(annotationDescr.getName(), true);
        if (annotationDescr.hasFields()) {
            visitAnnotations(av, annotationDescr.getFields());
        }
        annotationVisitors.add(av);
    }
    
    public void addField(String name, String type, int access) {
        FieldVisitor fv = cw.visitField(access, name, type, null, null);
        fieldVisitors.add(fv);
    }

    public void endFields() {
        for (FieldVisitor fv : fieldVisitors) {
            fv.visitEnd();
        }
    }
    
    public void endMethods()
    {
        for (int i=0; i<methodVisitors.size(); i++) {
            MethodVisitor mv = methodVisitors.get(i);
            mv.visitMaxs(0,0);
            mv.visitEnd();
        }
    }

    public void endClassAnnotations() {
        for (AnnotationVisitor av: annotationVisitors) {
            av.visitEnd();
        } 
    }

    public void write(OutputStream stream) throws IOException {
        cw.visit(Opcodes.V9, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, this.name, null, this.superclass, interfaces);
        AnnotationVisitor av = cw.visitAnnotation("Lorg/python/compiler/APIVersion;", true);
        av.visit("value", Integer.valueOf(Import.API_VERSION));
        av.visitEnd();

        av = cw.visitAnnotation("Lorg/python/compiler/MTime;", true);
        av.visit("value", Long.valueOf(mtime));
        av.visitEnd();

        if (sfilename != null) {
            av = cw.visitAnnotation("Lorg/python/compiler/Filename;", true);
            av.visit("value", sfilename);
            av.visitEnd();
            cw.visitSource(sfilename, null);
        }
        endClassAnnotations();
        endFields();
        endMethods();
//        final byte[] ba = cw.toByteArray();//CoroutineFixer.transform(cw.toByteArray());
        final byte[] ba = CoroutineFixer.transform(cw.toByteArray());
        stream.write(ba);
    }

}
