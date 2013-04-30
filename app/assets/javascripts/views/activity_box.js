// box moving and resizing in column view
$.widget("pt.activity_occurance_box", $.pt.column_box, {
  
  _create: function() {
    this.options = $.extend(this.options, {
      drag: _.bind(this.activity_box_moved,this),
      stop: _.bind(this.activity_box_moving_stop, this),
      start: this.options.occurance.get('start'),
      duration: this.options.occurance.get('duration')
    })
    this.element
      .attr('data-type', 'activity-occurance')
      .addClass('activity-occurance')
      .append(this.template);
    
    this.occurance = this.options.occurance;
      
    this.element.find('[data-source=name]').text(this.options.occurance.get("activity").get("name")); 
    this.element.find('[data-source=start]').text(this.options.occurance.get("start").format(this.activity_date_format));
    this.element.find('[data-source=end]').text( this.options.occurance.get("end").format(this.activity_date_format));

    this.element.find('a[data-button=activity-occurance-btn-edit]')
      .click(_.bind(_.partial(this._trigger,'edit', null, {occurance_id: this.occurance.get('id'), occurance: this.occurance, element:this.element}), this));
    this.element.find('a[data-button=activity-occurance-btn-remove]')
      .click(_.bind(_.partial(this._trigger,'remove', null, {occurance_id: this.occurance.get('id'), occurance: this.occurance, element:this.element}), this));
    
    this._super();      
  },
  
  template: 
    ""+
      "<div class='activity-occurance-inner'>" +
        "<div class='columnbox-handle-front activity-occurance-handle'></div>" +
        "<div class='columnbox-handle-back activity-occurance-handle'></div>" +
        "<div class='activity-occurance-labels'>" +
          "<div class='name' data-source='name'></div>" +
          "<div class='time'>" +
            "<span class='start' data-source='start'></span> - " +
            "<span class='end' data-source='end'></span>" +
          "</div>" +
          "<div class='buttons'>" +
            "<a href='#' data-button='activity-occurance-btn-edit'>Edit</a> " +
            "<a href='#' data-button='activity-occurance-btn-remove'>Delete</a>" +
          "</div>" +
        "</div>" +
      "</div>" +
    "",
 
  activity_date_format: 'DD.MM.YY H:mm',
  
  activity_box_moved: function(e, ui) {
    var $box = $(ui.el);
    var occurance = this.options.occurance;
    var column = this.column;
    
    occurance.set({
      start: ui.date_front,
      duration: ui.duration
    });
    
    $box.find('[data-source=start]').text(ui.date_front.format(this.activity_date_format));
    $box.find('[data-source=end]').text( occurance.get('end').format(this.activity_date_format));
    
    // align to new date time
    return true;
  },

  activity_box_moving_stop: function(e, ui) {
    this.occurance.save();
  },
});