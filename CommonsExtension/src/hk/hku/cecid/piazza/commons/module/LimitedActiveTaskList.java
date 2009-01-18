package hk.hku.cecid.piazza.commons.module;

import hk.hku.cecid.piazza.commons.util.StringUtilities;
import java.util.Properties;

public abstract class LimitedActiveTaskList extends ActiveTaskList
{
    private int maxTasksPerList;

    public LimitedActiveTaskList()
    {
    }

    protected void init()
        throws Exception
    {
        super.init();
        Properties params = getParameters();
        maxTasksPerList = StringUtilities.parseInt(params.getProperty("max-task-per-list"), 500);
    }

    public int getMaxTasksPerList()
    {
        return maxTasksPerList;
    }
}
