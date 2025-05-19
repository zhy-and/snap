package com.snap.intelligence.musics.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Java版本的OnlineSearchViewModel工厂类，用于在Java代码中创建Kotlin的OnlineSearchViewModel
 */
public class OnlineSearchViewModelJava {
    
    /**
     * 创建OnlineSearchViewModel的工厂方法
     */
    public static class Factory implements ViewModelProvider.Factory {
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            // 这里返回Kotlin版本的OnlineSearchViewModel实例
            // 由于Kotlin的ViewModel是通过Hilt注入的，这里我们需要通过反射创建一个实例
            try {
                // 返回一个空的ViewModel实例，实际的实例会由Hilt创建
                return modelClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}