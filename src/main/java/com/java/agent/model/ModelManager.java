package com.java.agent.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-model manager for Kode-cli Java implementation
 */
@Slf4j
@Component
public class ModelManager {
    private final List<ModelProfile> modelProfiles = new ArrayList<>();
    private final ModelPointers modelPointers = new ModelPointers();

    public String getMainModel() {
        return resolveModel(modelPointers.getMain());
    }

    public String getTaskModel() {
        String taskModel = resolveModel(modelPointers.getTask());
        return taskModel != null ? taskModel : getMainModel();
    }

    public String getReasoningModel() {
        String reasoningModel = resolveModel(modelPointers.getReasoning());
        return reasoningModel != null ? reasoningModel : getMainModel();
    }

    public String getQuickModel() {
        String quickModel = resolveModel(modelPointers.getQuick());
        return quickModel != null ? quickModel : getTaskModel();
    }

    public ModelProfile resolveModelProfile(String modelParam) {
        if (modelParam == null) return getDefaultProfile();

        switch (modelParam) {
            case "main": return findProfile(modelPointers.getMain());
            case "task": return findProfile(modelPointers.getTask());
            case "reasoning": return findProfile(modelPointers.getReasoning());
            case "quick": return findProfile(modelPointers.getQuick());
            default: return findProfile(modelParam);
        }
    }

    public void addModel(ModelProfile profile) {
        modelProfiles.removeIf(p -> p.getModelName().equals(profile.getModelName()));
        profile.setCreatedAt(System.currentTimeMillis());
        profile.setActive(true);
        modelProfiles.add(profile);

        if (modelProfiles.size() == 1) {
            modelPointers.setMain(profile.getModelName());
            modelPointers.setTask(profile.getModelName());
            modelPointers.setReasoning(profile.getModelName());
            modelPointers.setQuick(profile.getModelName());
        }
    }

    public void setPointer(String pointer, String modelName) {
        switch (pointer) {
            case "main": modelPointers.setMain(modelName); break;
            case "task": modelPointers.setTask(modelName); break;
            case "reasoning": modelPointers.setReasoning(modelName); break;
            case "quick": modelPointers.setQuick(modelName); break;
        }
    }

    public List<ModelProfile> getActiveProfiles() {
        return modelProfiles.stream()
                .filter(ModelProfile::isActive)
                .collect(Collectors.toList());
    }

    private String resolveModel(String modelName) {
        ModelProfile profile = findProfile(modelName);
        return profile != null ? profile.getModelName() : null;
    }

    private ModelProfile findProfile(String modelName) {
        if (modelName == null) return null;
        return modelProfiles.stream()
                .filter(p -> p.getModelName().equals(modelName) && p.isActive())
                .findFirst()
                .orElse(null);
    }

    private ModelProfile getDefaultProfile() {
        return modelProfiles.stream()
                .filter(ModelProfile::isActive)
                .findFirst()
                .orElse(null);
    }
}
