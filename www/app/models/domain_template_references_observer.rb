class DomainTemplateReferencesObserver < ActiveRecord::Observer
	observe :domain_template, :event, :activity

  def after_create(model)
    # check new referenced ids
    ids = model.referenced_domain_templates_ids

    DomainTemplate.where('id IN (?)', ids).update_all('reference_count = reference_count + 1')
  end

  def after_destroy(model)
    ids = model.referenced_domain_templates_ids

    DomainTemplate.where('id IN (?)', ids).update_all('reference_count = reference_count -1')
  end

  def after_update (model)
    return true unless model.referenced_domain_templates_ids_changed?

    # get referenced ids
    was_ids = model.referenced_domain_templates_ids_was
    now_ids = model.referenced_domain_templates_ids

    # make diff of before safe and after safe ids
    # and store that diff to counter

    removed_ids = was_ids.select {|id| not now_ids.include? id }
    same_ids = was_ids.select {|id| now_ids.include? id}
    new_ids = now_ids.select {|id| not was_ids.include? id} 
   
    # count referenced templates
    DomainTemplate.where('id IN (?)', removed_ids).update_all('reference_count = reference_count - 1')
    DomainTemplate.where('id IN (?)', new_ids).update_all('reference_count = reference_count + 1')

    return true
  end

end