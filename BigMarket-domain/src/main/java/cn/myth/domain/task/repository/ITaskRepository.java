package cn.myth.domain.task.repository;

import cn.myth.domain.task.model.entity.TaskEntity;

import java.util.List;

/**
 * 任务服务仓储接
 */
public interface ITaskRepository {

    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
