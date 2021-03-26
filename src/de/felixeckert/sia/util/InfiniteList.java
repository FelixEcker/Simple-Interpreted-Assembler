package de.felixeckert.sia.util;

import java.util.ArrayList;

public class InfiniteList<E> extends ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7095487347773722710L;

	@Override
	public E set(int index, E element) {
		if (size()-1 < index) {
			for (int i = size()-1; i < index+1; i++) {
				add(null);
			}
		}
		
		return super.set(index, element);
	}
}
