package com.workflow.engine.trigger;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Cron 定时触发器 - 类似 n8n 的 Schedule Trigger
 */
public class CronTrigger implements WorkflowTrigger {
    
    private static final Logger logger = LoggerFactory.getLogger(CronTrigger.class);
    
    private String id;
    private TriggerContext context;
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;
    private CronTrigger cronTrigger;
    
    public CronTrigger(String id, TaskScheduler taskScheduler) {
        this.id = id;
        this.taskScheduler = taskScheduler;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getType() {
        return "cron";
    }
    
    @Override
    public void initialize(TriggerContext context) {
        this.context = context;
        String cronExpression = context.getConfig().getCronExpression();
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression is required");
        }
        this.cronTrigger = new CronTrigger(cronExpression);
        logger.info("Initialized cron trigger with expression: {}", cronExpression);
    }
    
    @Override
    public void start() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            logger.warn("Cron trigger is already running");
            return;
        }
        
        scheduledFuture = taskScheduler.schedule(this::execute, cronTrigger);
        logger.info("Cron trigger started for workflow: {}", context.getWorkflowId());
    }
    
    @Override
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
            logger.info("Cron trigger stopped for workflow: {}", context.getWorkflowId());
        }
    }
    
    @Override
    public void trigger(Map<String, Object> payload) {
        // Cron 触发器自动执行，不需要手动触发
        logger.debug("Manual trigger not supported for cron trigger");
    }
    
    private void execute() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("triggerTime", Instant.now().toString());
            data.put("triggerType", "cron");
            data.put("cronExpression", context.getConfig().getCronExpression());
            
            String executionId = java.util.UUID.randomUUID().toString();
            context.getCallback().onTrigger(executionId, data);
            
            logger.info("Cron trigger executed for workflow: {}, executionId: {}", 
                context.getWorkflowId(), executionId);
        } catch (Exception e) {
            logger.error("Error executing cron trigger", e);
        }
    }
}
