package com.rethinkdb.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class TypeInfo {

	public final Annotation[] annotations;
	public final Class type;
	public final Type genericType;
	public final AnnotatedType annotatedType;

	public TypeInfo(Annotation[] annotations, Class type, Type genericType, AnnotatedType annotatedType) {
		this.annotations = annotations;
		this.type = type;
		this.genericType = genericType;
		this.annotatedType = annotatedType;
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass){
		for (Annotation annotation : annotations) {
			if(annotation.annotationType().equals(annotationClass)){
				return true;
			}
		}
		return false;
	}

	public static TypeInfo getTypeInfo(Field field){
		return new TypeInfo(field.getAnnotations(), field.getType(), field.getGenericType(), field.getAnnotatedType());
	}
}
