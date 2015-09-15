var React = require('react');

var Pager = React.createClass({
    render: function () {
        return (
            <nav>
                <ul className="pager">
                    <li>
                        <button className="btn btn-link" onClick={this.props.previous}>Previous</button>
                    </li>
                    <li>
                        <button className="btn btn-link" onClick={this.props.next}>Next</button>
                    </li>
                </ul>
            </nav>
        );
    }
});

var Digit = React.createClass({
    divStyle: {
        'text-align': 'center'
    },

    imgStyle: {
        width: '50%'
    },

    render: function () {
        return (
            <div className="well" style={this.divStyle}>
                <img src={this.props.digit()} className="img-thumbnail" style={this.imgStyle}/>

                <h1>{this.props.value}</h1>
            </div>
        );
    }
});

var App = React.createClass({
    getInitialState: function () {
        $.ajax({
            url: 'http://0.0.0.0/value/1',
            type: 'GET',
            dataType: 'json',
            cache: false,
            success: function (data) {
                this.setState({
                    value: data,
                    digit: 1
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.log(err);
            }.bind(this)
        });

        return {
            value: null,
            digit: 1
        };
    },

    digitLink: function () {
        return 'http://0.0.0.0/digits/' + this.state.digit;
    },

    previous: function () {
        var newDigit = this.state.digit - 1;
        $.ajax({
            url: 'http://0.0.0.0/value/' + newDigit,
            type: 'GET',
            dataType: 'json',
            cache: false,
            success: function (data) {
                this.setState({
                    value: data,
                    digit: newDigit
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.log(err);
            }.bind(this)
        });
    },

    next: function () {
        var newDigit = this.state.digit + 1;
        $.ajax({
            url: 'http://0.0.0.0/value/' + newDigit,
            type: 'GET',
            dataType: 'json',
            cache: false,
            success: function (data) {
                this.setState({
                    value: data,
                    digit: newDigit
                });
            }.bind(this),
            error: function (xhr, status, err) {
                console.log(err);
            }.bind(this)
        });
    },

    render: function () {
        return (
            <div>
                <Digit digit={this.digitLink} value={this.state.value}/>
                <Pager previous={this.previous} next={this.next}/>
            </div>
        );
    }
});

React.render(
    <App />,
    document.getElementById('content')
);
