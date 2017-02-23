package com.andraskindler.quickscroll;

/**
 * Interface required for FastTrack.
 * 
 * @author andraskindler
 *
 */
public interface Scrollable {

	/**
	 * This function returns the corresponding String to display at any given position
	 * <p>
	 * 
	 * @param childposition
	 *            equals childposition if used with ExpandableListView, position otherwise.
	 * @param groupposition
	 *            equals groupposition if used with ExpandableListView, zero otherwise.
	 */
	String getIndicatorForPosition(final int childposition, final int groupposition);

	/**
	 * This second function is responsible for is for implementing scroll behaviour. This can be used to perform special tasks, e.g. if you want to snap to the first item starting with a letter in an alphabetically ordered list or jump between groups in an ExpandableListView. If you want the normal approach, simply return childposition.
	 * <p>
	 * 
	 * @param childposition
	 *            equals childposition if used with ExpandableListView, position otherwise.
	 * @param groupposition
	 *            equals groupposition if used with ExpandableListView, zero otherwise.
	 */
	int getScrollPosition(final int childposition, final int groupposition);

}
