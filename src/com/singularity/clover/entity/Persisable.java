package com.singularity.clover.entity;

import java.util.ArrayList;


public interface Persisable{

	long getId();
	String getSchema();
	void store();
	void delete();
	Persisable load(long id);
	Persisable create();
	ArrayList<Long> loadTable(String whereClause,String[] whereArgs);
	/**
	 * ÓÃÓÚ²âÊÔÓÃÀı
	 */
	void clearTable();
}
