package ui.common;

import category.CategoryDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class CategorySelector {

  private final JPanel controls;
  private final JButton search;
  private final Map<Long, List<CategoryDTO>> childrenByParent = new LinkedHashMap<>();
  private final List<JComboBox<SelectOption>> boxes = new ArrayList<>();
  private boolean adjusting;

  public CategorySelector(JPanel controls, JButton search) {
    this.controls = controls;
    this.search = search;
  }

  public void load(List<CategoryDTO> categories) {
    childrenByParent.clear();
    boxes.clear();

    for (CategoryDTO category : categories) {
      childrenByParent
          .computeIfAbsent(category.getParentCategoryId(), key -> new ArrayList<>())
          .add(category);
    }
    addBox(null, false);
  }

  public Set<Long> selectedCategoryIds() {
    Long selectedId = selectedCategoryId();
    if (selectedId == null) {
      return java.util.Collections.emptySet();
    }

    Set<Long> ids = new HashSet<>();
    collectDescendantIds(selectedId, ids);
    return ids;
  }

  private void addBox(Long parentId, boolean includeAll) {
    List<CategoryDTO> children = childrenByParent.getOrDefault(parentId,
        java.util.Collections.emptyList());
    if (children.isEmpty()) {
      refreshControls();
      return;
    }

    JComboBox<SelectOption> box = new JComboBox<>();
    if (includeAll) {
      box.addItem(new SelectOption(0, "전체"));
    }
    for (CategoryDTO category : children) {
      box.addItem(new SelectOption(category.getCategoryId(), category.getCategoryName()));
    }
    box.addActionListener(event -> updateChildren(box));
    boxes.add(box);
    refreshControls();

    if (!includeAll) {
      updateChildren(box);
    }
  }

  private void updateChildren(JComboBox<SelectOption> changedBox) {
    if (adjusting) {
      return;
    }

    adjusting = true;
    try {
      int index = boxes.indexOf(changedBox);
      while (boxes.size() > index + 1) {
        boxes.remove(boxes.size() - 1);
      }

      SelectOption selected = (SelectOption) changedBox.getSelectedItem();
      if (selected != null && selected.getId() > 0 && hasChildren(selected.getId())) {
        addBox(selected.getId(), true);
      } else {
        refreshControls();
      }
    } finally {
      adjusting = false;
    }
  }

  private boolean hasChildren(long categoryId) {
    return !childrenByParent.getOrDefault(categoryId, java.util.Collections.emptyList()).isEmpty();
  }

  private Long selectedCategoryId() {
    Long selectedId = null;
    for (JComboBox<SelectOption> box : boxes) {
      SelectOption option = (SelectOption) box.getSelectedItem();
      if (option != null && option.getId() > 0) {
        selectedId = option.getId();
      }
    }
    return selectedId;
  }

  private void collectDescendantIds(Long categoryId, Set<Long> ids) {
    ids.add(categoryId);
    for (CategoryDTO child : childrenByParent.getOrDefault(categoryId,
        java.util.Collections.emptyList())) {
      collectDescendantIds(child.getCategoryId(), ids);
    }
  }

  private void refreshControls() {
    while (controls.getComponentCount() > 1) {
      controls.remove(1);
    }
    for (JComboBox<SelectOption> box : boxes) {
      controls.add(box);
    }
    controls.add(search);
    controls.revalidate();
    controls.repaint();
  }
}
