package com.tpt.chat_task.modules.resource.enums;

public enum RESOURCE_TYPE {
    IMAGE,
    AUDIO,
    VIDEO,
    FILE;

    public static RESOURCE_TYPE fromCloudinary(String resourceType) {
        if (resourceType == null) return FILE;
        return switch (resourceType.toLowerCase()) {
            case "image" -> IMAGE;
            case "video" -> VIDEO;
            case "audio" -> AUDIO;
            default -> FILE;
        };
    }
}
