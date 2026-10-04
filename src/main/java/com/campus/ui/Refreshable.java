package com.campus.ui;

/** Every tab implements this so MainFrame can reload its data when you switch tabs. */
public interface Refreshable {
    void refresh();
}