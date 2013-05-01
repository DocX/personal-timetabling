class CreateDomainTemplates < ActiveRecord::Migration
  def change
    create_table :domain_templates do |t|
      t.column :name, :string
      t.column :domain_data, :text
    end
  end

end
