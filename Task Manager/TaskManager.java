import java.util.ArrayList;
import java.util.Scanner;
class Task
{
    String name;
    String description;
    String dueDate;
    public Task(String name, String description, String dueDate)
    {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
    }
}
public class TaskManager
{
    public static void main(String[] args)
    {
        ArrayList<Task> taskList = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Task Management System:");
            System.out.println("1. Add Task");
            System.out.println("2. Remove Task");
            System.out.println("3. List Tasks");
            System.out.println("4. Exit");
            System.out.print("Select an option (1/2/3/4): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice)
            {
                case 1:
                    System.out.print("Enter task name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter task description: ");
                    String description = scanner.nextLine();
                    System.out.print("Enter due date: ");
                    String dueDate = scanner.nextLine();

                    Task newTask = new Task(name, description, dueDate);
                    taskList.add(newTask);
                    System.out.println("Task added successfully!");
                    break;

                case 2:
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to remove.");
                    } else {
                        System.out.println("Tasks:");
                        for (int i = 0; i < taskList.size(); i++)
                        {
                            System.out.println((i + 1) + ". " + taskList.get(i).name);
                        }
                        System.out.print("Enter the number of the task to remove: ");
                        int taskNumber = scanner.nextInt();
                        if (taskNumber > 0 && taskNumber <= taskList.size())
                        {
                            taskList.remove(taskNumber - 1);
                            System.out.println("Task removed successfully!");
                        } else {
                            System.out.println("Invalid task number.");
                        }
                    }
                    break;

                case 3:
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to list.");
                    }
                    else
                    {
                        System.out.println("Tasks:");
                        for (int i = 0; i < taskList.size(); i++)
                        {
                            Task task = taskList.get(i);
                            System.out.println((i + 1) + ". " + task.name);
                            System.out.println("   Description: " + task.description);
                            System.out.println("   Due Date: " + task.dueDate);
                        }
                    }
                    break;

                case 4:
                    System.out.println("Exiting Task Management System.");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option. Please select a valid option (1/2/3/4).");
            }
        }
    }
}
