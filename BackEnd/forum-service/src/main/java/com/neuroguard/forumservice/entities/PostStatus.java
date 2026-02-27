package com.neuroguard.forumservice.entities;

public enum PostStatus {
    PUBLISHED,     // visible
    PENDING,       // attente modération
    HIDDEN,        // caché par modérateur
    DELETED        // soft delete
}