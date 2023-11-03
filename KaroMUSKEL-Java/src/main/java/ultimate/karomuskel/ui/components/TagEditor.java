package ultimate.karomuskel.ui.components;

import java.awt.FlowLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import ultimate.karoapi4j.model.official.Tag;

public class TagEditor extends JPanel
{
    private static final long serialVersionUID = 1L;

	private List<Tag> suggestedTags;

	private Map<String, JToggleButton>			tagsButtons;
	private JTextField					tagsTF;

    public TagEditor(List<Tag> suggestedTags)
    {
        if(suggestedTags == null)
            throw new IllegalArgumentException("suggestedTags must not be null!");
        this.suggestedTags = suggestedTags;

        this.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.tagsButtons = new HashMap<>();
		
        JToggleButton tb;
        for(Tag tag: this.suggestedTags)
        {
            tb = new JToggleButton(tag.getLabel());
            this.tagsButtons.put(tag.getLabel(), tb);
            this.add(tb);
        }

        this.tagsTF = new JTextField(20);
        this.add(tagsTF);
    }

    public void setSelectedTags(Collection<String> tags)
    {        
        // reset all buttons & textfield
        this.tagsButtons.values().forEach(tb -> {
            tb.setSelected(false);
        });
        this.tagsTF.setText("");
        
        // set buttons & textfield
        if(tags != null)
        {
            Set<String> otherTags = new HashSet<>();
            for(String tag: tags)
            {           
                if(this.tagsButtons.containsKey(tag))
                    this.tagsButtons.get(tag).setSelected(true);
                else
                    otherTags.add(tag);
            }
            this.tagsTF.setText(String.join(", ", otherTags));
        }
    }

    public Set<String> getSelectedTags()
    {
		Set<String> tags = new LinkedHashSet<>();
		for(JToggleButton tb: this.tagsButtons.values())
		{
			if(tb.isSelected())
				tags.add(tb.getText());
		}
        tags.addAll(parseString(tagsTF.getText()));
		return tags;
    }

    public static Set<String> parseString(String tagString)
    {
        Set<String> tagSet = new LinkedHashSet<>();
        if(tagString != null)
        {
            String[] tagArray = tagString.split(",");
            for(String tag: tagArray)
            {
                if(!tag.trim().isEmpty())
                    tagSet.add(tag.trim());
            }
        }
        return tagSet;
    }
}
