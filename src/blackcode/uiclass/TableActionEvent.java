/* DON-CODE */
package blackcode.uiclass;

/**
 * This interface contain the Action for Edit and Delete buttons in note
 *
 * @author madfox99
 */
public interface TableActionEvent {

    /**
     * Action for Edit button
     *
     * @param row
     */
    public void onEdit(int row);

    /**
     * Action for Delete button
     *
     * @param row
     */
    public void onDelete(int row);
}
