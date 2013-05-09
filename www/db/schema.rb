# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20130509180914) do

  create_table "activities", :force => true do |t|
    t.string   "name",        :null => false
    t.text     "description"
    t.text     "data"
    t.datetime "created_at",  :null => false
    t.datetime "updated_at",  :null => false
  end

  create_table "domain_templates", :force => true do |t|
    t.string "name"
    t.text   "domain_data"
  end

  create_table "occurances", :force => true do |t|
    t.datetime "start",             :null => false
    t.integer  "duration",          :null => false
    t.integer  "activity_id"
    t.datetime "end"
    t.text     "domain_definition"
    t.integer  "min_duration"
    t.integer  "max_duration"
  end

end
